import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { PlantService } from '../../core/services/plant.service';
import { Plant } from '../../core/models/plant.model';
import { SensorService } from '../../core/services/sensor.service';
import { SensorData } from '../../core/models/sensor.model';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

interface PlantStats {
  plant: Plant;
  latest: SensorData | null;
  avgMoisture: number | null;
  avgLight: number | null;
  readings: number;
}

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, TranslateModule],
  templateUrl: './analytics.html',
  styleUrl: './analytics.scss',
})
export class Analytics implements OnInit {
  private plantSvc = inject(PlantService);
  private sensorSvc = inject(SensorService);

  plantStats = signal<PlantStats[]>([]);
  isLoading = signal(true);
  error = signal('');

  ngOnInit() {
    this.plantSvc.getMyPlants().subscribe({
      next: (plants) => {
        if (plants.length === 0) { this.isLoading.set(false); return; }

        const plantsWithDevices = plants.filter(p => p.deviceId);
        const plantsNoDevice = plants.filter(p => !p.deviceId).map(p =>
          ({ plant: p, latest: null, avgMoisture: null, avgLight: null, readings: 0 })
        );

        if (plantsWithDevices.length === 0) {
          this.plantStats.set(plantsNoDevice);
          this.isLoading.set(false);
          return;
        }

        const requests = plantsWithDevices.map(p =>
          this.sensorSvc.getSensorData(p.plantId, 50).pipe(catchError(() => of([])))
        );

        forkJoin(requests).subscribe((allData) => {
          const statsWithDevices: PlantStats[] = plantsWithDevices.map((plant, i) => {
            const data = allData[i] as SensorData[];
            const moistureData = data.filter(d => d.soilMoisture != null);
            const lightData = data.filter(d => d.lightIntensity != null);
            return {
              plant,
              latest: data[0] ?? null,
              avgMoisture: moistureData.length ? moistureData.reduce((s, d) => s + d.soilMoisture!, 0) / moistureData.length : null,
              avgLight: lightData.length ? lightData.reduce((s, d) => s + d.lightIntensity!, 0) / lightData.length : null,
              readings: data.length
            };
          });

          this.plantStats.set([...statsWithDevices, ...plantsNoDevice]);
          this.isLoading.set(false);
        });
      },
      error: () => { this.error.set('COMMON.ERRORS.LOAD_FAILED'); this.isLoading.set(false); }
    });
  }

  getMoistureStatus(plant: Plant, avg: number | null): 'ok' | 'low' | 'high' | 'unknown' {
    if (avg == null) return 'unknown';
    if (plant.optimalMoistureMin && avg < plant.optimalMoistureMin) return 'low';
    if (plant.optimalMoistureMax && avg > plant.optimalMoistureMax) return 'high';
    return 'ok';
  }
}

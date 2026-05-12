import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { PlantService } from '../../core/services/plant.service';
import { Plant, CreatePlantDto } from '../../core/models/plant.model';
import { DeviceService } from '../../core/services/device.service';
import { Device } from '../../core/models/device.model';
import { SensorService } from '../../core/services/sensor.service';
import { SensorData } from '../../core/models/sensor.model';

@Component({
  selector: 'app-plants-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, TranslateModule, DatePipe],
  templateUrl: './plants-list.html',
  styleUrl: './plants-list.scss'
})
export class PlantsList implements OnInit {
  private plantService = inject(PlantService);
  private deviceService = inject(DeviceService);
  private sensorService = inject(SensorService);

  plants = signal<Plant[]>([]);
  devices = signal<Device[]>([]);
  sensorMap = signal<Record<number, SensorData>>({});
  isLoading = signal(true);
  error = signal('');
  showAddForm = signal(false);
  editingPlant = signal<Plant | null>(null);
  editForm: any = {};

  newPlant: CreatePlantDto = {
    name: '',
    species: '',
    wateringMode: 0,
    optimalMoistureMin: 40,
    optimalMoistureMax: 80,
    optimalLightMin: 200,
    optimalLightMax: 800,
  };

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.isLoading.set(true);
    this.plantService.getMyPlants().subscribe({
      next: (plants) => {
        this.plants.set(plants);
        this.isLoading.set(false);
        // Load latest sensor data for each plant
        plants.forEach(p => {
          if (p.deviceId) {
            this.sensorService.getSensorData(p.plantId, 1).subscribe({
              next: (data) => {
                if (data.length > 0) {
                  const m = { ...this.sensorMap() };
                  m[p.plantId] = data[0];
                  this.sensorMap.set(m);
                }
              }
            });
          }
        });
      },
      error: () => {
        this.error.set('COMMON.ERRORS.LOAD_FAILED');
        this.isLoading.set(false);
      }
    });

    this.deviceService.getMyDevices().subscribe({
      next: (d) => this.devices.set(d)
    });
  }

  addPlant() {
    if (!this.newPlant.name) return;
    this.plantService.createPlant(this.newPlant).subscribe({
      next: () => {
        this.showAddForm.set(false);
        this.newPlant = { name: '', species: '', wateringMode: 0, optimalMoistureMin: 40, optimalMoistureMax: 80 };
        this.loadAll();
      },
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

  deletePlant(id: number) {
    if (!confirm('Видалити цю рослину?')) return;
    this.plantService.deletePlant(id).subscribe({
      next: () => this.loadAll(),
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

  startEdit(plant: Plant) {
    this.editingPlant.set(plant);
    this.editForm = {
      name: plant.name,
      species: plant.species ?? '',
      deviceId: plant.deviceId,
      wateringMode: plant.wateringMode,
      optimalMoistureMin: plant.optimalMoistureMin,
      optimalMoistureMax: plant.optimalMoistureMax,
      optimalLightMin: plant.optimalLightMin,
      optimalLightMax: plant.optimalLightMax,
      notes: plant.notes ?? '',
      isActive: plant.isActive,
    };
  }

  cancelEdit() { this.editingPlant.set(null); }

  saveEdit() {
    const p = this.editingPlant();
    if (!p || !this.editForm.name) return;
    this.plantService.updatePlant(p.plantId, this.editForm).subscribe({
      next: () => { this.editingPlant.set(null); this.loadAll(); },
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

  getMoistureStatus(plant: Plant, sensor?: SensorData): 'ok' | 'low' | 'high' | 'unknown' {
    if (!sensor?.soilMoisture) return 'unknown';
    const m = sensor.soilMoisture;
    if (plant.optimalMoistureMin && m < plant.optimalMoistureMin) return 'low';
    if (plant.optimalMoistureMax && m > plant.optimalMoistureMax) return 'high';
    return 'ok';
  }

  getDeviceName(id?: number): string {
    if (!id) return '—';
    return this.devices().find(d => d.deviceId === id)?.name ?? `#${id}`;
  }

  getMoistureLabel(status: 'ok' | 'low' | 'high' | 'unknown'): string {
    const labels: Record<string, string> = {
      ok: 'Норма',
      low: 'Занизька',
      high: 'Зависока',
      unknown: '—'
    };
    return labels[status];
  }
}

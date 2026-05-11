import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { PlantService, Plant } from '../../../services/plant.service';
import { WateringService, WateringEvent, WateringSchedule } from '../../../services/sensor.service';
import { FormsModule } from '@angular/forms';

interface PlantEvents {
  plant: Plant;
  events: WateringEvent[];
  schedules: WateringSchedule[];
  expanded: boolean;
}

@Component({
  selector: 'app-watering',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, TranslateModule],
  templateUrl: './watering.html',
  styleUrl: './watering.scss',
})
export class Watering implements OnInit {
  private plantSvc = inject(PlantService);
  private wateringSvc = inject(WateringService);

  plantData = signal<PlantEvents[]>([]);
  isLoading = signal(true);
  error = signal('');
  
  showScheduleForm = signal<{plantId: number, idx: number} | null>(null);
  newSchedule: any = { startTime: '08:00', intervalHours: 24, amountMl: 100, enabled: true, daysOfWeek: [1,2,3,4,5,6,0] };

  readonly MODE_LABELS: Record<number, string> = {
    0: '🤲 Ручний',
    1: '🤖 Автоматичний (по датчикам)',
    2: '📅 Автоматичний (за розкладом)'
  };
  readonly STATUS_LABELS: Record<number, string> = {
    0: '⏳ Заплановано',
    1: '✅ Виконано',
    2: '❌ Скасовано'
  };
  readonly STATUS_CLASSES: Record<number, string> = {
    0: 'status-pending',
    1: 'status-done',
    2: 'status-cancelled'
  };
  readonly DAYS = ['Нд', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'];

  ngOnInit() {
    this.plantSvc.getMyPlants().subscribe({
      next: (plants) => {
        const entries: PlantEvents[] = plants.map(p => ({ plant: p, events: [], schedules: [], expanded: true }));
        this.plantData.set(entries);
        this.isLoading.set(false);

        // load events and schedules per plant
        entries.forEach((entry, idx) => {
          this.wateringSvc.getEvents(entry.plant.plantId).subscribe({
            next: (events) => {
              const updated = [...this.plantData()];
              updated[idx] = { ...updated[idx], events: events.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()) };
              this.plantData.set(updated);
            }
          });
          this.wateringSvc.getSchedules(entry.plant.plantId).subscribe({
            next: (schedules) => {
              const updated = [...this.plantData()];
              updated[idx] = { ...updated[idx], schedules };
              this.plantData.set(updated);
            }
          });
        });
      },
      error: () => { this.error.set('COMMON.ERRORS.LOAD_FAILED'); this.isLoading.set(false); }
    });
  }

  toggle(idx: number) {
    const updated = [...this.plantData()];
    updated[idx] = { ...updated[idx], expanded: !updated[idx].expanded };
    this.plantData.set(updated);
  }

  deleteSchedule(plantIdx: number, scheduleId: number) {
    if (!confirm('Видалити розклад поливу?')) return;
    this.wateringSvc.deleteSchedule(scheduleId).subscribe({
      next: () => {
        const updated = [...this.plantData()];
        updated[plantIdx] = { ...updated[plantIdx], schedules: updated[plantIdx].schedules.filter(s => s.scheduleId !== scheduleId) };
        this.plantData.set(updated);
      }
    });
  }

  totalWatered(events: WateringEvent[]): number {
    return events.filter(e => e.status === 1).reduce((sum, e) => sum + e.amountMl, 0);
  }

  updateWateringMode(idx: number, plantId: number, mode: number) {
    const p = this.plantData()[idx].plant;
    const dto = { ...p, wateringMode: mode };
    this.plantSvc.updatePlant(plantId, dto).subscribe({
      next: () => {
        const updated = [...this.plantData()];
        updated[idx].plant.wateringMode = mode;
        this.plantData.set(updated);
      }
    });
  }

  openScheduleForm(plantId: number, idx: number) {
    this.showScheduleForm.set({plantId, idx});
    this.newSchedule = { startTime: '08:00', intervalHours: 24, amountMl: 100, enabled: true, daysOfWeek: [1,2,3,4,5,6,0] };
  }

  closeScheduleForm() { this.showScheduleForm.set(null); }

  saveSchedule() {
    const s = this.showScheduleForm();
    if (!s) return;
    
    // Convert time format HH:mm to HH:mm:ss for backend if needed
    let st = this.newSchedule.startTime;
    if (st.length === 5) st += ':00';
    
    const dto = { ...this.newSchedule, plantId: s.plantId, startTime: st };
    this.wateringSvc.createSchedule(dto).subscribe({
      next: (sch) => {
        const updated = [...this.plantData()];
        updated[s.idx].schedules.push(sch);
        this.plantData.set(updated);
        this.closeScheduleForm();
      },
      error: () => alert('Помилка при збереженні розкладу')
    });
  }
}

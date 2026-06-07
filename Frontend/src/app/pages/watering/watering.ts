import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { PlantService } from '../../core/services/plant.service';
import { Plant, WateringMode } from '../../core/models/plant.model';
import { WateringService } from '../../core/services/sensor.service';
import { WateringEvent, WateringSchedule } from '../../core/models/sensor.model';
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
  newSchedule: any = { startTime: '08:00', intervalHours: 24, amountMl: 100, repeatCount: 1, enabled: true, daysOfWeek: [] };
  wateringPlantIdx = signal<number | null>(null);

  readonly MODE_LABELS: Record<string, string> = {
    'Manual': '🤲 Ручний',
    'Automatic': '🤖 Автоматичний (по датчикам)',
    'Scheduled': '📅 Автоматичний (за розкладом)'
  };
  readonly STATUS_LABELS: Record<string, string> = {
    'Completed': '✅ Виконано',
    'Failed': '❌ Не вдалося',
    'Cancelled': '❌ Скасовано'
  };
  readonly STATUS_CLASSES: Record<string, string> = {
    'Completed': 'status-done',
    'Failed': 'status-cancelled',
    'Cancelled': 'status-cancelled'
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
    return events.filter(e => e.status === 'Completed').reduce((sum, e) => sum + e.amountMl, 0);
  }

  updateWateringMode(idx: number, plantId: number, mode: WateringMode) {
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

  toggleDay(day: number) {
    const days: number[] = [...this.newSchedule.daysOfWeek];
    const i = days.indexOf(day);
    if (i === -1) days.push(day); else days.splice(i, 1);
    this.newSchedule = { ...this.newSchedule, daysOfWeek: days };
  }

  hasDay(day: number): boolean {
    return this.newSchedule.daysOfWeek.includes(day);
  }

  openScheduleForm(plantId: number, idx: number) {
    this.showScheduleForm.set({plantId, idx});
    this.newSchedule = { startTime: '08:00', intervalHours: 24, amountMl: 100, repeatCount: 1, enabled: true, daysOfWeek: [] };
  }

  waterNow(idx: number) {
    const entry = this.plantData()[idx];
    this.wateringPlantIdx.set(idx);
    this.wateringSvc.createEvent({
      plantId: entry.plant.plantId,
      amountMl: 200,
      mode: 'Manual',
      status: 'Completed'
    }).subscribe({
      next: (ev) => {
        const updated = [...this.plantData()];
        updated[idx] = { ...updated[idx], events: [ev, ...updated[idx].events] };
        this.plantData.set(updated);
        this.wateringPlantIdx.set(null);
      },
      error: () => { this.wateringPlantIdx.set(null); this.error.set('COMMON.ERRORS.GENERAL'); }
    });
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

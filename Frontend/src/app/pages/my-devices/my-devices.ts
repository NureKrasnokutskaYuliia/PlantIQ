import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DeviceService } from '../../core/services/device.service';
import { Device, CreateDeviceDto } from '../../core/models/device.model';
import { PlantService } from '../../core/services/plant.service';
import { Plant } from '../../core/models/plant.model';
import { forkJoin } from 'rxjs';

const STATUS_LABELS: Record<number, string> = { 0: '🟢 Онлайн', 1: '🔴 Офлайн', 2: '⚠️ Помилка' };
const STATUS_CLASSES: Record<number, string> = { 0: 'status-online', 1: 'status-offline', 2: 'status-error' };

@Component({
  selector: 'app-my-devices',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, TranslateModule],
  templateUrl: './my-devices.html',
  styleUrl: './my-devices.scss'
})
export class MyDevices implements OnInit {
  private deviceService = inject(DeviceService);
  private plantService = inject(PlantService);

  devices = signal<Device[]>([]);
  plantMap = signal<Record<number, string>>({});
  isLoading = signal(true);
  error = signal('');
  showAddForm = signal(false);

  newDevice: CreateDeviceDto = { name: '', model: '', serialNumber: '' };

  ngOnInit() { this.load(); }

  load() {
    this.isLoading.set(true);
    const userId = Number(localStorage.getItem('userId'));
    forkJoin({
      devices: this.deviceService.getMyDevices(),
      plants: this.plantService.getMyPlants()
    }).subscribe({
      next: (res) => {
        this.devices.set(res.devices);
        
        // Map plant names to deviceIds
        const pMap: Record<number, string> = {};
        res.plants.forEach(p => {
          if (p.deviceId) pMap[p.deviceId] = p.name;
        });
        this.plantMap.set(pMap);
        
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('COMMON.ERRORS.LOAD_FAILED');
        this.isLoading.set(false);
      }
    });
  }

  add() {
    if (!this.newDevice.name) return;
    this.deviceService.createDevice(this.newDevice).subscribe({
      next: () => { this.showAddForm.set(false); this.newDevice = { name: '', model: '', serialNumber: '' }; this.load(); },
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

  delete(id: number) {
    if (!confirm('Видалити пристрій?')) return;
    this.deviceService.deleteDevice(id).subscribe({
      next: () => this.load(),
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

  getStatusLabel(s: number) { return STATUS_LABELS[s] ?? '—'; }
  getStatusClass(s: number) { return STATUS_CLASSES[s] ?? ''; }
}

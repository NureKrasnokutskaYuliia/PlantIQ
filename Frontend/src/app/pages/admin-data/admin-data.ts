import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService } from '../../core/services/admin.service';
import { PlantService } from '../../core/services/plant.service';
import { Plant } from '../../core/models/plant.model';
import { DeviceService } from '../../core/services/device.service';
import { Device } from '../../core/models/device.model';

@Component({
  selector: 'app-admin-data',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './admin-data.html',
  styleUrl: './admin-data.scss'
})
export class AdminData implements OnInit {
  private admin = inject(AdminService);
  private plantSvc = inject(PlantService);
  private deviceSvc = inject(DeviceService);

  stats = signal<any>(null);
  isLoading = signal(true);

  ngOnInit() {
    this.admin.getStats().subscribe({
      next: (s) => { this.stats.set(s); this.isLoading.set(false); },
      error: () => this.isLoading.set(false)
    });
  }
}

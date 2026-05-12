export interface Plant {
  plantId: number;
  userId: number;
  deviceId?: number;
  name: string;
  species?: string;
  optimalMoistureMin?: number;
  optimalMoistureMax?: number;
  optimalLightMin?: number;
  optimalLightMax?: number;
  notes?: string;
  createdAt: string;
  isActive: boolean;
  wateringMode: number;
}

export interface CreatePlantDto {
  name: string;
  deviceId?: number;
  species?: string;
  optimalMoistureMin?: number;
  optimalMoistureMax?: number;
  optimalLightMin?: number;
  optimalLightMax?: number;
  notes?: string;
  wateringMode: number;
}

export type WateringMode = 'Manual' | 'Automatic' | 'Scheduled';

export interface PlantSpecies {
  speciesId: number;
  name: string;
  defaultMoistureMin?: number;
  defaultMoistureMax?: number;
  defaultLightMin?: number;
  defaultLightMax?: number;
}

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
  wateringMode: WateringMode;
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
  wateringMode: WateringMode;
}

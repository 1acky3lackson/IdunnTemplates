/// <reference types="vite/client" />

// 声明 schematic-renderer 模块
// 如果该库自带类型定义，则不需要这部分；如果没有，这可以防止 TS 报错
declare module 'schematic-renderer' {
    export class SchematicRenderer {
        constructor(
            canvas: HTMLCanvasElement,
            schematicData: Record<string, () => Promise<ArrayBuffer>>,
            resourcePacks: Record<string, any>,
            options: any
        );
        cameraManager: {
            switchCameraPreset: (preset: string) => void;
            focusOnSchematics: () => void;
        };
        schematicManager: {
            loadSchematicFromURL: (url: string, id: string) => Promise<void>;
        };
        setIsometricAngles: (pitch: number, yaw: number) => void;
    }
}

// === Type Definitions ===
interface CameraOptions {
    alpha?: number;  // Horizontal rotation (radians)
    beta?: number;   // Vertical rotation (radians)
    radius?: number; // Zoom level (distance from target)
}

// === 类型定义 ===
declare global {
    interface Window {
        // Updated signature to accept options
        renderSchematic: (
            url: string, 
            width: number, 
            height: number, 
            options?: CameraOptions
        ) => Promise<void>;
    }
}

export {};
package _Minecraft2.render;

public class Frustum {
    private float[][] planes = new float[6][4]; // 6 plans, chaque plan = [A, B, C, D]

    // Appelle cette fonction à chaque frame
    public void extractFrustum(float[] matrix) {
        // matrix = projection * view (16 floats, colonne major)

        // LEFT
        planes[0] = new float[] {
            matrix[3] + matrix[0],
            matrix[7] + matrix[4],
            matrix[11] + matrix[8],
            matrix[15] + matrix[12]
        };

        // RIGHT
        planes[1] = new float[] {
            matrix[3] - matrix[0],
            matrix[7] - matrix[4],
            matrix[11] - matrix[8],
            matrix[15] - matrix[12]
        };

        // BOTTOM
        planes[2] = new float[] {
            matrix[3] + matrix[1],
            matrix[7] + matrix[5],
            matrix[11] + matrix[9],
            matrix[15] + matrix[13]
        };

        // TOP
        planes[3] = new float[] {
            matrix[3] - matrix[1],
            matrix[7] - matrix[5],
            matrix[11] - matrix[9],
            matrix[15] - matrix[13]
        };

        // NEAR
        planes[4] = new float[] {
            matrix[3] + matrix[2],
            matrix[7] + matrix[6],
            matrix[11] + matrix[10],
            matrix[15] + matrix[14]
        };

        // FAR
        planes[5] = new float[] {
            matrix[3] - matrix[2],
            matrix[7] - matrix[6],
            matrix[11] - matrix[10],
            matrix[15] - matrix[14]
        };

        // Normaliser les plans
        for (int i = 0; i < 6; i++) {
            float length = (float) Math.sqrt(
                planes[i][0] * planes[i][0] +
                planes[i][1] * planes[i][1] +
                planes[i][2] * planes[i][2]
            );
            for (int j = 0; j < 4; j++) {
                planes[i][j] /= length;
            }
        }
    }

    // Vérifie si un cube est dans le frustum
    public boolean isBoxInFrustum(float x, float y, float z, float size) {
        for (int i = 0; i < 6; i++) {
            float a = planes[i][0];
            float b = planes[i][1];
            float c = planes[i][2];
            float d = planes[i][3];

            // 8 sommets du cube
            boolean in = false;
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dy = -1; dy <= 1; dy += 2) {
                    for (int dz = -1; dz <= 1; dz += 2) {
                        float px = x + dx * size;
                        float py = y + dy * size;
                        float pz = z + dz * size;
                        if (a * px + b * py + c * pz + d > 0) {
                            in = true;
                            break;
                        }
                    }
                    if (in) break;
                }
                if (in) break;
            }

            if (!in) return false;
        }

        return true;
    }

	public boolean isBoxInFrustum(float[] boundingBox) {
		return isBoxInFrustum(boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[0] - boundingBox[3]);
	}
}

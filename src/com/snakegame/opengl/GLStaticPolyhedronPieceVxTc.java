package com.snakegame.opengl;

public class GLStaticPolyhedronPieceVxTc extends GLStaticPolyhedronPieceBase {
    public GLStaticPolyhedronPieceVxTc(GLTexture diffuseTexture, float[] vertices, float[] texCoordinates) {
        super(diffuseTexture, vertices.length / 3);

        m_VboIds = new int[2];

        m_VboIds[0] = createVbo();
        copyFloatDataIntoVbo(0, 3, vertices);

        m_VboIds[1] = createVbo();
        copyFloatDataIntoVbo(1, 2, texCoordinates);
    }

    public void draw() {
        drawTriangles(2);
    }
}

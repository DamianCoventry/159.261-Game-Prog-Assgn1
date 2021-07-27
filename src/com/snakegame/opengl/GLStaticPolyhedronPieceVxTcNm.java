package com.snakegame.opengl;

public class GLStaticPolyhedronPieceVxTcNm extends GLStaticPolyhedronPieceBase {
    public GLStaticPolyhedronPieceVxTcNm(GLTexture diffuseTexture, float[] vertices, float[] texCoordinates, float[] normals) {
        super(diffuseTexture, vertices.length / 3);

        m_VboIds = new int[3];

        m_VboIds[0] = createVbo();
        copyFloatDataIntoVbo(0, 3, vertices);

        m_VboIds[1] = createVbo();
        copyFloatDataIntoVbo(1, 2, texCoordinates);

        m_VboIds[2] = createVbo();
        copyFloatDataIntoVbo(2, 3, normals);
    }

    public void draw() {
        drawTriangles(3);
    }
}

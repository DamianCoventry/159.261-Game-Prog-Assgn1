package com.snakegame.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * This class opens and parses a Wavefront .obj file. The data from the file are saved into private members of this
 * class. The users of this class can use the public accessors to read the data.
 * */

// https://en.wikipedia.org/wiki/Wavefront_.obj_file
public class ObjFile {
    private final ArrayList<Object> m_Objects;
    private final ArrayList<String> m_MaterialNames;
    private final ArrayList<Vertex> m_Vertices;
    private final ArrayList<TexCoordinate> m_TexCoordinates;
    private final ArrayList<Vertex> m_Normals;

    public static class Object {
        private final String m_Name;
        private final ArrayList<Face> m_Faces;
        private String m_MaterialName;
        private int m_SmoothingGroup;
        public Object(String name) {
            m_Name = name;
            m_Faces = new ArrayList<>();
        }
        public String getName() {
            return m_Name;
        }
        public ArrayList<Face> getFaces() {
            return m_Faces;
        }
        public void addFace(Face face) {
            m_Faces.add(face);
        }
        public void setMaterialName(String name) {
            m_MaterialName = name;
        }
        public String getMaterialName() {
            return m_MaterialName;
        }
        public void setSmoothingGroup(int shading) {
            m_SmoothingGroup = shading;
        }
        public int getSmoothingGroup() {
            return m_SmoothingGroup;
        }
    }

    public static class Vertex {
        public float m_X, m_Y, m_Z;
        public Vertex(float x, float y, float z) {
            m_X = x;
            m_Y = y;
            m_Z = z;
        }
    }

    public static class TexCoordinate {
        public float m_S, m_T;
        public TexCoordinate(float s, float t) {
            m_S = s;
            m_T = t;
        }
    }

    public static class Face {
        public int[] m_Vertices;
        public int[] m_TexCoordinates;
        public int[] m_Normals;
        public Face(int[] vertices, int[] texCoordinates, int[] normals) {
            m_Vertices = vertices;
            m_TexCoordinates = texCoordinates;
            m_Normals = normals;
        }
    }

    public ObjFile(String fileName) throws Exception {
        m_Objects = new ArrayList<>();
        m_MaterialNames = new ArrayList<>();
        m_Vertices = new ArrayList<>();
        m_TexCoordinates = new ArrayList<>();
        m_Normals = new ArrayList<>();

        File file = new File(fileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = bufferedReader.readLine();
        while (line != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                parseLine(line);
            }
            line = bufferedReader.readLine();
        }
    }

    public ArrayList<Object> getObjects() {
        return m_Objects;
    }
    public ArrayList<String> getMaterialNames() {
        return m_MaterialNames;
    }
    public ArrayList<Vertex> getVertices() {
        return m_Vertices;
    }
    public ArrayList<TexCoordinate> getTexCoordinates() {
        return m_TexCoordinates;
    }
    public ArrayList<Vertex> getNormals() {
        return m_Normals;
    }

    private void parseLine(String line) throws Exception {
        String[] words = line.split("\\s");
        if (words[0].equals("mtllib")) {
            parseMaterial(words);
        }
        else if (words[0].equals("o")) {
            parseObject(words);
        }
        else if (words[0].equals("v")) {
            parseVertex(words);
        }
        else if (words[0].equals("vt")) {
            parseTexCoordinate(words);
        }
        else if (words[0].equals("vn")) {
            parseNormal(words);
        }
        else if (words[0].equals("usemtl")) {
            parseMaterialAssignment(words);
        }
        else if (words[0].equals("s")) {
            parseSmoothingGroup(words);
        }
        else if (words[0].equals("f")) {
            parseFace(words);
        }
    }

    private void parseObject(String[] words) {
        if (words.length == 2) {
            m_Objects.add(new Object(words[1]));
        }
    }

    private void parseMaterial(String[] words) {
        if (words.length == 2) {
            m_MaterialNames.add(words[1]);
        }
    }

    private void parseVertex(String[] words) {
        if (words.length == 4) {
            m_Vertices.add(new Vertex(
                    Float.parseFloat(words[1]),
                    Float.parseFloat(words[2]),
                    Float.parseFloat(words[3])
            ));
        }
    }

    private void parseTexCoordinate(String[] words) {
        if (words.length == 3) {
            m_TexCoordinates.add(new TexCoordinate(
                    Float.parseFloat(words[1]),
                    Float.parseFloat(words[2])
            ));
        }
    }

    private void parseNormal(String[] words) {
        if (words.length == 4) {
            m_Normals.add(new Vertex(
                    Float.parseFloat(words[1]),
                    Float.parseFloat(words[2]),
                    Float.parseFloat(words[3])
            ));
        }
    }

    private void parseMaterialAssignment(String[] words) {
        if (words.length == 2 && !m_Objects.isEmpty()) {
            m_Objects.get(m_Objects.size() - 1).setMaterialName(words[1]);
        }
    }

    private void parseSmoothingGroup(String[] words) {
        if (words.length == 2 && !words[1].equals("off") && !m_Objects.isEmpty()) {
            m_Objects.get(m_Objects.size() - 1).setSmoothingGroup(Integer.parseInt(words[1]));
        }
    }

    private void parseFace(String[] words) throws Exception {
        if (words.length != 4 && !m_Objects.isEmpty()) {
            throw new Exception("Only triangles supported");
        }
        int[] triplet0 = parseInteger3(words[1]);
        int[] triplet1 = parseInteger3(words[2]);
        int[] triplet2 = parseInteger3(words[3]);
        int[] vertices = new int[3];
        vertices[0] = triplet0[0]; vertices[1] = triplet1[0]; vertices[2] = triplet2[0];
        int[] texCoordinates = new int[3];
        texCoordinates[0] = triplet0[1]; texCoordinates[1] = triplet1[1]; texCoordinates[2] = triplet2[1];
        int[] normals = new int[3];
        normals[0] = triplet0[2]; normals[1] = triplet1[2]; normals[2] = triplet2[2];
        m_Objects.get(m_Objects.size() - 1).addFace(
                new Face(vertices, texCoordinates, normals)
        );
    }

    private int[] parseInteger3(String triplet) throws Exception {
        String[] indices = triplet.split("/");
        if (indices.length != 3) {
            throw new Exception("Invalid face triplet");
        }
        int[] integer3 = new int[3];
        // Note that indices within the file are 1 based, NOT 0 based.
        integer3[0] = Integer.parseInt(indices[0]) - 1;
        integer3[1] = Integer.parseInt(indices[1]) - 1;
        integer3[2] = Integer.parseInt(indices[2]) - 1;
        return integer3;
    }
}

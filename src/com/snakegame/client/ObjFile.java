//
// Snake Game
// https://en.wikipedia.org/wiki/Snake_(video_game_genre)
//
// Based on the 1976 arcade game Blockade, and the 1991 game Nibbles
// https://en.wikipedia.org/wiki/Blockade_(video_game)
// https://en.wikipedia.org/wiki/Nibbles_(video_game)
//
// This implementation is Copyright (c) 2021, Damian Coventry
// All rights reserved
// Written for Massey University course 159.261 Game Programming (Assignment 1)
//

package com.snakegame.client;

import java.io.*;
import java.util.ArrayList;

/**
 * This class opens and parses a Wavefront .obj file. The data from the file are saved into private members of this
 * class. The users of this class can use the public accessors to read the data.
 * */

// https://en.wikipedia.org/wiki/Wavefront_.obj_file
public class ObjFile {
    private final ArrayList<Object> m_Objects;
    private final ArrayList<String> m_MaterialFileNames;
    private final ArrayList<Vertex> m_Vertices;
    private final ArrayList<TexCoordinate> m_TexCoordinates;
    private final ArrayList<Vertex> m_Normals;

    public static class Piece {
        private final String m_MaterialName;
        private final ArrayList<Face> m_Faces;
        public Piece(String materialName) {
            m_MaterialName = materialName;
            m_Faces = new ArrayList<>();
        }
        public String getMaterialName() {
            return m_MaterialName;
        }
        public ArrayList<Face> getFaces() {
            return m_Faces;
        }
        public void addFace(Face face) {
            m_Faces.add(face);
        }
    }

    public static class Object {
        private final String m_Name;
        private final ArrayList<Piece> m_Pieces;
        private int m_SmoothingGroup;
        public Object(String name) {
            m_Name = name;
            m_Pieces = new ArrayList<>();
        }
        public String getName() {
            return m_Name;
        }
        public ArrayList<Piece> getPieces() {
            return m_Pieces;
        }
        public void addPiece(Piece piece) {
            m_Pieces.add(piece);
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
        public float m_U, m_V;
        public TexCoordinate(float s, float t) {
            m_U = s;
            m_V = t;
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
        m_MaterialFileNames = new ArrayList<>();
        m_Vertices = new ArrayList<>();
        m_TexCoordinates = new ArrayList<>();
        m_Normals = new ArrayList<>();

        BufferedReader bufferedReader = null;
        try {
	        File file = new File(fileName);
	        bufferedReader = new BufferedReader(new FileReader(file));
	        String line = bufferedReader.readLine();
	        while (line != null) {
	            line = line.trim();
	            if (!line.isEmpty() && !line.startsWith("#")) {
	                parseLine(line);
	            }
	            line = bufferedReader.readLine();
	        }
        }
        finally {
        	if (bufferedReader != null) {
        		bufferedReader.close();
        	}
        }
    }

    public ArrayList<Object> getObjects() {
        return m_Objects;
    }
    public ArrayList<String> getMaterialFileNames() {
        return m_MaterialFileNames;
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
        String[] words = line.split(" ");
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
            parseUseMaterial(words);
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
            m_MaterialFileNames.add(words[1]);
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

    private void parseUseMaterial(String[] words) {
        if (words.length == 2 && !m_Objects.isEmpty()) {
            getCurrentObject().addPiece(new Piece(words[1]));
        }
    }

    private void parseSmoothingGroup(String[] words) {
        if (words.length == 2 && !words[1].equals("off") && !m_Objects.isEmpty()) {
            getCurrentObject().setSmoothingGroup(Integer.parseInt(words[1]));
        }
    }

    private void parseFace(String[] words) throws Exception {
        if (words.length != 4 && !m_Objects.isEmpty()) {
            throw new Exception("Only triangles supported");
        }

        int[] vertex0 = parseInteger3(words[1]);
        int[] vertex1 = parseInteger3(words[2]);
        int[] vertex2 = parseInteger3(words[3]);

        int[] vertices = new int[3];
        vertices[0] = vertex0[0];
        vertices[1] = vertex1[0];
        vertices[2] = vertex2[0];

        int[] texCoordinates = new int[3];
        texCoordinates[0] = vertex0[1];
        texCoordinates[1] = vertex1[1];
        texCoordinates[2] = vertex2[1];

        int[] normals = new int[3];
        normals[0] = vertex0[2];
        normals[1] = vertex1[2];
        normals[2] = vertex2[2];

        getCurrentPiece().addFace(new Face(vertices, texCoordinates, normals));
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

    private Object getCurrentObject() {
        if (m_Objects == null || m_Objects.size() == 0) {
            throw new RuntimeException("There is no current object");
        }
        return m_Objects.get(m_Objects.size() - 1);
    }

    private Piece getCurrentPiece() {
        Object object = getCurrentObject();
        if (object.getPieces() == null || object.getPieces().size() == 0) {
            throw new RuntimeException("There is no current object");
        }
        return object.getPieces().get(object.getPieces().size() - 1);
    }
}

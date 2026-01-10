package com.example.service;

import com.example.exception.NoSquareException;
import com.example.model.Matrix;

public class MatrixMathematics {

    private MatrixMathematics(){}

    public static double determinant(Matrix matrix) throws NoSquareException {
        if (!matrix.isSquare())
            throw new NoSquareException("matrix need to be square.");
        if (matrix.size() == 1) return matrix.getValueAt(0, 0);

        if (matrix.size() == 2) {
            return (matrix.getValueAt(0, 0) * matrix.getValueAt(1, 1)) - (matrix.getValueAt(0, 1) * matrix.getValueAt(1, 0));
        }
        double sum = 0.0;
        for (int i=0; i<matrix.getNcols(); i++) {
            sum += changeSign(i) * matrix.getValueAt(0, i) * determinant(createSubMatrix(matrix, 0, i));
        }
        return sum;
    }

    private static int changeSign(int i) {
        return (i % 2 == 0) ? 1 : -1;
    }

    public static Matrix createSubMatrix(Matrix matrix, int excludingRow, int excludingCol) {
        Matrix mat = new Matrix(matrix.getNrows() - 1, matrix.getNcols() - 1);
        int r = -1;
        for (int i = 0; i < matrix.getNrows(); i++) {
            if (i == excludingRow) continue;
            r++;
            int c = -1;
            for (int j = 0; j < matrix.getNcols(); j++) {
                if (j == excludingCol) continue;
                mat.setValueAt(r, ++c, matrix.getValueAt(i, j));
            }
        }
        return mat;
    }

    public static Matrix cofactor(Matrix matrix) throws NoSquareException {
        Matrix mat = new Matrix(matrix.getNrows(), matrix.getNcols());
        for (int i=0; i<matrix.getNrows(); i++) {
            for (int j=0; j<matrix.getNcols(); j++) {
                mat.setValueAt(i, j, changeSign(i) * changeSign(j) * determinant(createSubMatrix(matrix, i, j)));
            }
        }
        return mat;
    }

    public static Matrix transpose(Matrix matrix) {
        Matrix transposedMatrix = new Matrix(matrix.getNcols(), matrix.getNrows());
        for (int i=0; i<matrix.getNrows(); i++) {
            for (int j=0; j<matrix.getNcols(); j++) {
                transposedMatrix.setValueAt(j, i, matrix.getValueAt(i, j));
            }
        }
        return transposedMatrix;
    }

    public static Matrix inverse(Matrix matrix) throws NoSquareException {
        double det = determinant(matrix);
        // Correction du BUG CRITIQUE : Division par zéro
        if (det == 0) {
            throw new IllegalStateException("Determinant is zero, matrix has no inverse.");
        }
        return (transpose(cofactor(matrix)).multiplyByConstant(1.0 / det));
    }
}
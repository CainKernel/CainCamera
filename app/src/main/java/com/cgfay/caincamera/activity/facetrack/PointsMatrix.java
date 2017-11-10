package com.cgfay.caincamera.activity.facetrack;

import android.opengl.GLES30;

import com.cgfay.caincamera.utils.GlUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class PointsMatrix {
	private final String vertexShaderCode =
			"uniform mat4 uMVPMatrix;" +
			"attribute vec4 vPosition;" +
			"void main() {" +
			"  gl_Position = vPosition * uMVPMatrix; " +
			"  gl_PointSize = 8.0;" +
			"}";

	private final String fragmentShaderCode =
			"precision mediump float;" +
			"uniform vec4 vColor;" +
			"void main() {" +
			"  gl_FragColor = vColor;" +
			"}";

	private final int mProgramHandle;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;

	float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

	float color_rect[] = { 0X61 / 255.0f, 0XB3 / 255.0f, 0X4D / 255.0f, 1.0f };

	// 画点
	public ArrayList<ArrayList> points = new ArrayList<ArrayList>();

	public PointsMatrix() {
		mProgramHandle = GlUtil.createProgram(vertexShaderCode, fragmentShaderCode);
	}

	public void draw(float[] mvpMatrix) {
		// Add program to OpenGL environment
		GLES30.glUseProgram(mProgramHandle);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "vPosition");

		// get handle to fragment shader's vColor member
		mColorHandle = GLES30.glGetUniformLocation(mProgramHandle, "vColor");

		// get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
		GlUtil.checkGlError("glGetUniformLocation");

		// Apply the projection and view transformation
		GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		GlUtil.checkGlError("glUniformMatrix4fv");
		// Enable a handle to the triangle vertices
		GLES30.glEnableVertexAttribArray(mPositionHandle);
		// Set color for drawing the triangle
		GLES30.glUniform4fv(mColorHandle, 1, color_rect, 0);

		GLES30.glUniform4fv(mColorHandle, 1, color, 0);

		synchronized (this) {
			for (int i = 0; i < points.size(); i++) {
				ArrayList<FloatBuffer> triangleVBList = points.get(i);
				for (int j = 0; j < triangleVBList.size(); j++) {
					FloatBuffer fb = triangleVBList.get(j);
					if (fb != null) {
						GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false, 0, fb);
						// Draw the point
						GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);
					}
				}
			}
		}

		// Disable vertex array
		GLES30.glDisableVertexAttribArray(mPositionHandle);
	}

}

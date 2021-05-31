package com.jozufozu.flywheel.core;

import static org.lwjgl.opengl.GL20.glDrawArrays;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.util.AttribUtil;

public class BufferedModel {

	protected final GlPrimitive primitiveMode;
	protected final ByteBuffer data;
	protected final VertexFormat format;
	protected final int vertexCount;
	protected GlBuffer vbo;
	protected boolean deleted;

	public BufferedModel(GlPrimitive primitiveMode, VertexFormat format, ByteBuffer data, int vertices) {
		this.primitiveMode = primitiveMode;
		this.data = data;
		this.format = format;
		this.vertexCount = vertices;

		vbo = new GlBuffer(GlBufferType.ARRAY_BUFFER);

		vbo.bind();
		// allocate the buffer on the gpu
		vbo.alloc(this.data.capacity());

		// mirror it in system memory so we can write to it, and upload our model.
		vbo.getBuffer(0, this.data.capacity())
				.put(this.data)
				.flush();
		vbo.unbind();
	}

	public VertexFormat getFormat() {
		return format;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public void bindBuffer() {
		vbo.bind();
	}

	public void unbindBuffer() {
		vbo.unbind();
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public void draw() {
		if (vertexCount <= 0 || deleted) return;

		vbo.bind();

		AttribUtil.enableArrays(getAttributeCount());
		format.vertexAttribPointers(0);

		glDrawArrays(primitiveMode.glEnum, 0, vertexCount);

		AttribUtil.disableArrays(getAttributeCount());

		vbo.unbind();
	}

	/**
	 * Draws many instances of this model, assuming the appropriate state is already bound.
	 */
	public void drawInstances(int instanceCount) {
		if (vertexCount <= 0 || deleted) return;

		Backend.compat.drawInstanced.drawArraysInstanced(primitiveMode, 0, vertexCount, instanceCount);
	}

	public void delete() {
		if (deleted) return;

		deleted = true;
		vbo.delete();
	}

	public int getAttributeCount() {
		return format.getAttributeCount();
	}

}

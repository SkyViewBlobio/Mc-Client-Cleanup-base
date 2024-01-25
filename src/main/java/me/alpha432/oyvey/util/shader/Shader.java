package me.alpha432.oyvey.util.shader;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.*;

public abstract class Shader {
    public int program;
    public Map<String, Integer> uniformsMap;
    protected String fragmentShader;

    public Shader(String fragmentShader) {
        this.fragmentShader = fragmentShader;
        int fragmentShaderID;
        int vertexShaderID;

        try {
            InputStream vertexStream = getClass().getResourceAsStream("/shader/vertex.vert");
            vertexShaderID = createShader(IOUtils.toString(vertexStream, Charset.defaultCharset()), ARBVertexShader.GL_VERTEX_SHADER_ARB);
            IOUtils.closeQuietly(vertexStream);

            InputStream fragmentStream = getClass().getResourceAsStream("/shader/" + fragmentShader);
            fragmentShaderID = createShader(IOUtils.toString(fragmentStream, Charset.defaultCharset()), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
            IOUtils.closeQuietly(fragmentStream);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (vertexShaderID == 0 || fragmentShaderID == 0) {
            return;
        }
        this.program = ARBShaderObjects.glCreateProgramObjectARB();
        if (this.program == 0) return;

        ARBShaderObjects.glAttachObjectARB(this.program, vertexShaderID);
        ARBShaderObjects.glAttachObjectARB(this.program, fragmentShaderID);

        ARBShaderObjects.glLinkProgramARB(this.program);
        ARBShaderObjects.glValidateProgramARB(this.program);
    }

    public void startShader() {
        GlStateManager.pushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap<String, Integer>();
            setupUniforms();
        }
        updateUniforms();
    }

    public void stopShader() {
        GL20.glUseProgram(0);
        GlStateManager.popMatrix();
    }

    public abstract void setupUniforms();

    public abstract void updateUniforms();

    public int createShader(String shaderSource, int shaderType) {
        int shader = 0;

        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
            if (shader == 0) return 0;

            ARBShaderObjects.glShaderSourceARB(shader, shaderSource);
            ARBShaderObjects.glCompileShaderARB(shader);

            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));
            }
            return shader;
        }
        catch (Exception e) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            throw e;
        }
    }

    public String getLogInfo(final int i) {
        return ARBShaderObjects.glGetInfoLogARB(i, ARBShaderObjects.glGetObjectParameteriARB(i, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    public void setUniform(String uniformName, int location) {
        uniformsMap.put(uniformName, location);
    }

    public void setupUniform(String uniformName) {
        setUniform(uniformName, GL20.glGetUniformLocation(program, uniformName));
    }

    public int getUniform(String uniformName) {
        return uniformsMap.get(uniformName);
    }

    public int getProgramId() {
        return this.program;
    }
}
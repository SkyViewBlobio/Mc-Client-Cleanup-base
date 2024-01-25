package me.alpha432.oyvey.util.shader;

@FunctionalInterface
public interface ShaderProducer {
    FramebufferShader getInstance();
}

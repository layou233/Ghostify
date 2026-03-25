package com.launium.ghostify.client.util;

import com.launium.ghostify.client.GhostifyClient;
import com.launium.ghostify.client.ui.animation.Animation;
import com.launium.ghostify.client.ui.animation.Smooth;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class MusicInstance extends AbstractSoundInstance implements TickableSoundInstance {
    public final String resourcePath;
    private final Animation volume = new Smooth(0F, 1F);
    private final boolean forceRepeat;
    private final long stopAt;

    private long lastTickTime;

    public MusicInstance(String resourceID, String resourcePath, boolean forceRepeat, long stopAt) {
        super(ResourceLocation.fromNamespaceAndPath("ghostify", resourceID),
                SoundSource.MASTER, SoundInstance.createUnseededRandom());
        this.resourcePath = resourcePath;
        this.forceRepeat = this.looping = forceRepeat;
        this.stopAt = stopAt;
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream oggInput = new FileInputStream(resourcePath);
                return repeatInstantly || forceRepeat
                        ? new LoopingAudioStream(JOrbisAudioStream::new, oggInput)
                        : new JOrbisAudioStream(oggInput);
            } catch (IOException e) {
                GhostifyClient.LOGGER.warn("[Ghostify] Cannot load {}.", resourcePath, e);
                throw new CompletionException(e);
            }
        }, Util.ioPool());
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public float getVolume() {
        return volume.current * super.getVolume();
    }

    @Override
    public boolean isStopped() {
        return volume.current < 0.004F && Util.getMillis() > stopAt;
    }

    @Override
    public void tick() {
        long now = Util.getMillis();
        long timeDiff = now - lastTickTime;
        lastTickTime = now;
        if (timeDiff > 40) timeDiff = 40;

        if (now > stopAt) {
            volume.target = 0F;
        }
        volume.tick(timeDiff * 0.001F);
    }
}

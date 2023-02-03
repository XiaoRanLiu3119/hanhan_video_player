package com.lxr.video_player.widget.subtitle;

import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.List;

import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.exo2.demo.EventLogger;

public class GSYExoSubTitlePlayer extends IjkExo2MediaPlayer {

    private String mSubTitile;
    private Player.Listener mTextOutput;

    public GSYExoSubTitlePlayer(Context context) {
        super(context);
    }


    @Override
    public void onCues(List<Cue> cues) {
        super.onCues(cues);
        /// 这里
    }

    @Override
    public void onMetadata(Metadata metadata) {
        super.onMetadata(metadata);
        /// 这里
    }

    @Override
    protected void prepareAsyncInternal() {
        new Handler(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (mTrackSelector == null) {
                            mTrackSelector = new DefaultTrackSelector(mAppContext);
                        }
                        mEventLogger = new EventLogger(mTrackSelector);
                        boolean preferExtensionDecoders = true;
                        boolean useExtensionRenderers = true;//是否开启扩展
                        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = useExtensionRenderers
                                ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
                        if (mRendererFactory == null) {
                            mRendererFactory = new DefaultRenderersFactory(mAppContext);
                            mRendererFactory.setExtensionRendererMode(extensionRendererMode);
                        }
                        if (mLoadControl == null) {
                            mLoadControl = new DefaultLoadControl();
                        }
                        mInternalPlayer = new ExoPlayer.Builder(mAppContext, mRendererFactory)
                                .setLooper(Looper.getMainLooper())
                                .setTrackSelector(mTrackSelector)
                                .setLoadControl(mLoadControl).build();
                        mInternalPlayer.addListener(GSYExoSubTitlePlayer.this);
                        mInternalPlayer.addAnalyticsListener(GSYExoSubTitlePlayer.this);
                        if (mTextOutput != null) {
                            mInternalPlayer.addListener(mTextOutput);
                        }
                        mInternalPlayer.addListener(mEventLogger);
                        if (mSpeedPlaybackParameters != null) {
                            mInternalPlayer.setPlaybackParameters(mSpeedPlaybackParameters);
                        }
                        if (mSurface != null)
                            mInternalPlayer.setVideoSurface(mSurface);

                        if (mSubTitile != null) {
                            MediaSource textMediaSource = getTextSource(Uri.parse(mSubTitile));
                            mMediaSource = new MergingMediaSource(mMediaSource, textMediaSource);
                        }
                        mInternalPlayer.setMediaSource(mMediaSource);
                        mInternalPlayer.prepare();
                        mInternalPlayer.setPlayWhenReady(false);
                    }
                }
        );
    }

    public MediaSource getTextSource(Uri subTitle) {
        //todo C.SELECTION_FLAG_AUTOSELECT language MimeTypes
        Format textFormat = new Format.Builder()
                /// 其他的比如 text/x-ssa ，text/vtt，application/ttml+xml 等等
                .setSampleMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setSelectionFlags(C.SELECTION_FLAG_FORCED)
                /// 如果出现字幕不显示，可以通过修改这个语音去对应，
                //  这个问题在内部的 selectTextTrack 时，TextTrackScore 通过 getFormatLanguageScore 方法判断语言获取匹配不上
                //  就会不出现字幕
                .setLanguage("en")
                .build();

        MediaItem.SubtitleConfiguration  subtitle = new MediaItem.SubtitleConfiguration.Builder(subTitle)
            .setMimeType(checkNotNull(textFormat.sampleMimeType))
            .setLanguage( textFormat.language)
            .setSelectionFlags(textFormat.selectionFlags).build();

        DefaultHttpDataSource.Factory  factory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(50000)
                .setReadTimeoutMs(50000)
                .setTransferListener( new DefaultBandwidthMeter.Builder(mAppContext).build());

        MediaSource textMediaSource = new SingleSampleMediaSource.Factory(new DefaultDataSource.Factory(mAppContext,
                factory))
                .createMediaSource(subtitle, C.TIME_UNSET);
        return textMediaSource;

    }


    public String getSubTitile() {
        return mSubTitile;
    }

    public void setSubTitile(String subTitile) {
        this.mSubTitile = subTitile;
    }

    public Player.Listener getTextOutput() {
        return mTextOutput;
    }

    public void setTextOutput(Player.Listener textOutput) {
        this.mTextOutput = textOutput;
    }

    public void addTextOutputPlaying(Player.Listener textOutput) {
        if (mInternalPlayer != null) {
            mInternalPlayer.addListener(textOutput);
        }
    }

    public void removeTextOutput(Player.Listener textOutput) {
        if (mInternalPlayer != null) {
            mInternalPlayer.removeListener(textOutput);
        }
    }

}

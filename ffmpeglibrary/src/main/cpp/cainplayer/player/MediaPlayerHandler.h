//
// Created by cain on 2018/5/1.
//

#ifndef CAINPLAYER_MEDIAPLAYERHANDLER_H
#define CAINPLAYER_MEDIAPLAYERHANDLER_H

#include <Handler.h>
#include <android/native_window.h>
#include "AVMediaPlayer.h"

typedef enum {
    kMsgPlayerSetDataSource,
    kMsgPlayerSetSurface,
    kMsgPlayerSetAudioChannel,
    kMsgPlayerPrepare,
    kMsgPlayerStart,
    kMsgPlayerSeek,
    kMsgPlayerStop,
    kMsgPlayerPause,
    kMsgPlayerResume
} PlayerOperationType;

// 播放器释放完成回调
typedef void playerReleaseCallback(void);

class MediaPlayerHandler : public Handler {

public:
    MediaPlayerHandler(AVMediaPlayer *mediaPlayer, MessageQueue *queue);

    virtual ~MediaPlayerHandler();

    void handleMessage(Message *msg) override;

    void setMediaPlayer(AVMediaPlayer *player);

    void setPlayerReleaseCallback(playerReleaseCallback *callback);

    void setSurface(ANativeWindow *nativeWindow);

    void setAudioStream(int index);

    void prepare();

    void start();

    void seek(int64_t sec);

    void stop();

    void pause();

    void resume();

private:
    AVMediaPlayer *mediaPlayer;
    playerReleaseCallback *mCallback;
};


#endif //CAINPLAYER_MEDIAPLAYERHANDLER_H


# 添加源文件
add_library( metadata_retriever

             SHARED

             # library
             source/MediaMetadataRetriever.cpp
             source/Metadata.cpp

             # controller
             MetadataController.cpp )

# 链接静态库
target_link_libraries( metadata_retriever

                       ffmpeg

                       android
                       jnigraphics
                       log )


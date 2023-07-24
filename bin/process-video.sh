#!/usr/bin/env bash

# Supposing Java 17 (or more) and ffmpeg are in the PATH, that the
# subtitles have been extracted in SRT format from the video file,
# that the file time segments contains the time segments to extract,
# set the following environment variables to get a single MKV containing the
# video file.

# /!\ the destination file and temporary video files are created in the current directory

# ffmpeg options; only options related to video and audio transformation
FFMPEG_OPT='-map 0:1 -s 1280x720 -b:v 1750k -c:v h264_nvenc -map 0:2 -map 0:3 -c:a copy'

# subtitle options, shift and/extend display times
SUBPROCESSOR_OPT='-e 20 -s 500'

SOURCE_VIDEO=video.ts

SOURCE_SRT=subtitles.srt

SOURCE_TIME_SEGMENT=time.segments

SUBPROCESSOR_JAR=subfileprocessor.jar

DESTINATION_VIDEO=my_r.mkv
TITLE="Video Title"

cmd_ffmpeg=ffmpeg
cmd_mkvmerge=mkvmerge

## =======

set -e

sub_output=sub.srt
java -jar "$SUBPROCESSOR_JAR" -o "$sub_output" $SUBPROCESSOR_OPT "$SOURCE_SRT" "$SOURCE_TIME_SEGMENT"

# Extract videos
readarray -t time_segments < "$SOURCE_TIME_SEGMENT"

for i in "${!time_segments[@]}"; do
  printf "%s\t%s\n" "$i" "${time_segments[$i]}"
  prev_ifs=$IFS
  IFS=' --> '
  read -r -a start_end <<< "${time_segments[$i]}"
  IFS=$prev_ifs

  destination_file=v${i}.ts

  "$cmd_ffmpeg" -hide_banner -y -i "$SOURCE_VIDEO" $FFMPEG_OPT -ss "${start_end[0]//,/.}" -to "${start_end[3]//,/.}" "${destination_file}"

  if [[ $i == 0 ]]; then
    files_to_merge+=" ${destination_file}"
  else
    files_to_merge+=" +${destination_file}"
  fi
done

# Merge with subtitles
"$cmd_mkvmerge" --disable-track-statistics-tags --title "$TITLE" -o "$DESTINATION_VIDEO" ${files_to_merge} "${sub_output}"

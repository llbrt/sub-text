# Subtitle text

This project generates a subtitle file (format SRT) from an existing one. Three operations can be done:
- extend the display time of each subtitle (modify end time);
- shift the start and end time of each subtitle;
- keep only the subtitles included in a few time segments; the start and end of each duration is
updated to maintain the synchronization with the source video.


# Build

This project requires [Maven](https://maven.apache.org/) and a JDK version 17.

To build the project, launch the command

```
mvn clean package -DskipTests=true
```

The jar `target/subfileprocessor.jar` is autonomous: it can be copied anywhere.

# Usage

```
> java -jar target/subfileprocessor.jar
Missing required parameter: '<subtitles>'
Usage: subfileprocessor [-e=<durationIncrement>] [-o=<outputFile>]
                        [-s=<shiftStart>] <subtitles> [<segments>]
      <subtitles>            Path to the subtitle file
      [<segments>]           File containing the list of segments to keep (one
                               per line, same time format as srt file)
  -e, --extend=<durationIncrement>
                             Extend subtitle duration (in milliseconds)
  -o, --output=<outputFile>  Output file
  -s, --shift=<shiftStart>   Shift subtitle start (in milliseconds)
```

If no output file is provided, the output file is `<subtitles>.new.srt`.

The script `bin/process-video.sh` is an example of this project can be used to split
and merge a TS file to a MKV file. It requires `ffmpeg` and `mkvmerge`.

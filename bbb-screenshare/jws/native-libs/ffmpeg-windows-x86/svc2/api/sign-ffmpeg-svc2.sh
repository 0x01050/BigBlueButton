FFMPEG=ffmpeg-3.0.2-1.2-svc2.jar
if [ -d "workdir" ]; then
  rm -rf workdir
fi
mkdir workdir
cp $FFMPEG workdir
cd workdir
jar xf $FFMPEG
rm $FFMPEG
rm -rf META-INF
jar cf ffmpeg.jar *
cd ..
ant sign-ffmpeg-jar
cp workdir/ffmpeg.jar ../../../../../app/jws/lib/ffmpeg-win-x86-svc2.jar
rm -rf workdir


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;

import javazoom.jl.player.Player;

public class Music implements Runnable
{
	String Route = null;
	public Music(String Route)
	{
		this.Route = Route;
	}
	private static void play(String position)
	{
		
		getDuration(position);
		try
		{
			BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(position));
			Player player = new Player(buffer);
			player.play();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static int getDuration(String position)
	{

		int length = 0;
		try
		{
			MP3File mp3File = (MP3File) AudioFileIO.read(new File(position));
			MP3AudioHeader audioHeader = (MP3AudioHeader) mp3File.getAudioHeader();

			// 单位为秒
			length = audioHeader.getTrackLength();

			return length;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return length;
	}
	public void run()
	{
		play(Route);
	}
}
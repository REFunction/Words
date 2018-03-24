import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class words
{
	static String WordRoute = "words.txt";
	static HashMap<String, Word> WordsMap = new HashMap<>();
	static int LearnTimes = 3;
	static AtomicInteger TotalNum = new AtomicInteger(0);
	static int FinishedNum = 0;
	static int MaxThreadNum = 8;
	static AtomicInteger NowThreadNum = new AtomicInteger(0);

	public static void ReadOnlyWordsFromFile()
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(WordRoute));
			String line = null;
			while ((line = br.readLine()) != null)
			{
				if (line.length() > 0 && !line.equals("\n"))
					TotalNum.getAndIncrement();
			}
			MaxThreadNum = TotalNum.get();
			if (MaxThreadNum > 30)
				MaxThreadNum = 30;
			if (MaxThreadNum <= 0)
				MaxThreadNum = 1;

			GUI.ProcessBar.setMaximum(TotalNum.get());
			br.close();
			br = new BufferedReader(new FileReader(WordRoute));
			while ((line = br.readLine()) != null)
			{
				if (line.length() == 0 || line.equals("\n"))
					continue;
				String English = line;
				while (NowThreadNum.get() >= MaxThreadNum)
					Thread.sleep(300);
				new Thread(new WordSearchOnline(English)).start();
				GUI.ProcessBar.setValue(WordsMap.size());
			}
			br.close();
			while (NowThreadNum.get() != 0)
			{
				Thread.sleep(300);
				GUI.ProcessBar.setValue(WordsMap.size());
			}
			GUI.ProcessBar.setValue(0);

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static boolean IsChinese(String str)
	{
		String regEx = "[\u4e00-\u9fa5]";
		Pattern pat = Pattern.compile(regEx);
		Matcher matcher = pat.matcher(str);
		boolean flg = false;
		if (matcher.find())
			flg = true;

		return flg;
	}

	public static String GetChinese(String word)
	{
		String result = "";
		for (String s : WordsMap.get(word).Chinese)
		{
			result = result + s + "\n";
		}
		return result;
	}

	public static String GetPronounce(String word)
	{
		return WordsMap.get(word).Pronounce;
	}

	public static String GetNextWord(String LastWord)
	{
		if (TotalNum.get() == 0)
			return "No words.txt";
		if (WordsMap.keySet().size() == 1)
		{
			return WordsMap.keySet().iterator().next();
		}
		if (WordsMap.keySet().size() == 0)
		{
			return "All Finished";
		}

		String SelectWord = "Error";
		Random random = new Random(System.currentTimeMillis());
		int SelectNum = (int) (random.nextDouble() * WordsMap.size());
		while (true)
		{
			for (String s : WordsMap.keySet())
			{
				if (SelectNum == 0)
				{
					SelectWord = s;
					break;
				}
				SelectNum--;
			}
			if (!SelectWord.equals(LastWord) && !SelectWord.equals("Error"))
				break;
			SelectNum = (int) (Math.random() * WordsMap.size());
		}
		return SelectWord;
	}

	public static void Yes(String word)
	{
		WordsMap.get(word).RemainingNum--;
	}

	public static void No(String word)
	{
		WordsMap.get(word).RemainingNum = LearnTimes;
	}

	public static void Delete(String word)
	{
		WordsMap.remove(word);
		FinishedNum++;
		GUI.SetProcess(FinishedNum);
	}

	public static Word SearchWordOnline(String word)
	{
		Word w = new Word(word);
		String content = null; 
		String s = null;
		do
		{			
			s = Http.httpRequest("http://dict.youdao.com/w/eng/" + word + "/#keyfrom=dict2.index", "GET", null);
			if (s.contains("您要找的是不是"))
			{
				return null;
			}
				
			content = s.split("trans-container")[1];
		}while(!content.contains("<li>"));
		
		for (int i = 1; i < content.split("<li>").length; i++)
		{
			String Chinese = content.split("<li>")[i];
			Chinese = Chinese.replaceAll(" ", "");
			Chinese = Chinese.replaceAll("</li>", "");
			Chinese = Chinese.replaceAll("	", "");
			Chinese = FilterString(Chinese);

			w.Chinese.add(Chinese);
		}
		//获取发音
		File file = new File("sound/" + word + ".mp3");
		if(!file.exists())
		{
			String info = Http.httpRequest("http://www.iciba.com/" + word, "GET", null);
			if (info.contains("sound"))
			{
				String [] Strings = info.split("sound");
				String Mp3Route = null;
				for(int i = 0;i < Strings.length;i++)
				{
					if(Strings[i].charAt(2) == 'h')
					{
						Mp3Route = Strings[i];
						break;
					}
				}

				try
				{
					Mp3Route = Mp3Route.substring(0, Mp3Route.lastIndexOf("mp3")) + "mp3";
					Mp3Route = Mp3Route.substring(Mp3Route.indexOf("http"),Mp3Route.length());
					Http.DownLoadFromUrl(Mp3Route, word + ".mp3", "sound");
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				System.out.println(word + " : " + info);
				System.exit(0);
			}
		}
		
		// phonetic
		content = FilterString(s.split("phonetic")[2]);
		if (!content.contains("[")) // 短语没有音标
		{
			System.out.println(w.Pronounce);
			return w;
		}
		content = content.replaceAll(">", "");
		content = content.replaceAll("\"", "");
		w.Pronounce = "[" + content.split("\\[")[1].split("\\]")[0] + "]";
		return w;
	}

	public static void Speak(String word)
	{
		new Thread(new Music("sound\\" + word + ".mp3")).start();;
	}
	public static String SearchWord(String word)
	{
		String result = "";
		String s = Http.httpRequest("http://dict.youdao.com/w/eng/" + word + "/#keyfrom=dict2.index", "GET", null);
		if (s.contains("您要找的是不是"))
			return "不存在";
		String content = s.split("trans-container")[1];
		for (int i = 1; i < content.split("<li>").length; i++)
		{
			String Chinese = content.split("<li>")[i];
			Chinese = Chinese.replaceAll(" ", "");
			Chinese = Chinese.replaceAll("</li>", "");
			Chinese = Chinese.replaceAll("	", "");
			Chinese = FilterString(Chinese);
			result = result + Chinese + "\n";
		}
		return result;
	}

	private static String FilterString(String s)
	{
		if ((!s.contains("<")) && (!s.contains(">")))
			return s;
		int start = s.indexOf('<');
		s = s.substring(0, start);
		return s;
	}
	private static void WriteToHtml(String content,String filename)
	{
		try
		{
			BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(filename));
			bo.write(content.getBytes());
			bo.close();
		} catch (Exception e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}
	
}

class Word
{
	String English = null;
	HashSet<String> Chinese = new HashSet<>();
	String Pronounce = "";
	int RemainingNum = 1;

	public Word(String word)
	{
		English = word;
	}

	public Word()
	{

	}
}
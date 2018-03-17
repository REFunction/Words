import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class words
{
	static String WordRoute = "words.txt";
	static HashMap<String, Word> WordsMap = new HashMap<>();
	static int LearnTimes = 3;
	static int TotalNum = 0;
	static int FinishedNum = 0;
	static int MaxThreadNum = 8;
	static int NowThreadNum = 0;

	public static void ReadOnlyWordsFromFile()
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(WordRoute));
			String line = null;
			while ((line = br.readLine()) != null)
			{
				if (line.length() > 0)
					TotalNum++;
			}
			MaxThreadNum = TotalNum / 3;
			if (MaxThreadNum > 30)
				MaxThreadNum = 30;
			if (MaxThreadNum <= 0)
				MaxThreadNum = 1;

			GUI.ProcessBar.setMaximum(TotalNum);
			br.close();
			br = new BufferedReader(new FileReader(WordRoute));
			while ((line = br.readLine()) != null)
			{
				if (line.length() == 0)
					continue;
				String English = line;
				while (NowThreadNum >= MaxThreadNum)
					Thread.sleep(300);
				new Thread(new WordSearchOnline(English)).start();
				GUI.ProcessBar.setValue(WordsMap.size());
				Thread.sleep(100);
			}
			br.close();
			while (NowThreadNum != 0)
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
		if (TotalNum == 0)
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
			if (!SelectWord.equals(LastWord))
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
		String s = Http.httpRequest("http://dict.youdao.com/w/eng/" + word + "/#keyfrom=dict2.index", "GET", null);
		if (s.contains("您要找的是不是"))
			return null;
		String content = s.split("trans-container")[1];
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
		String info = Http.httpRequest("http://www.iciba.com/" + word, "GET", null);
		if (info.contains("sound"))
		{
			String Mp3Route = info.split("sound")[1];
			Mp3Route = Mp3Route.substring(0, Mp3Route.lastIndexOf("mp3")) + "mp3";
			Mp3Route = Mp3Route.substring(Mp3Route.indexOf("http"),Mp3Route.length());
			try
			{
				Http.DownLoadFromUrl(Mp3Route, word + ".mp3", "sound");
			} catch (IOException e)
			{
				e.printStackTrace();
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
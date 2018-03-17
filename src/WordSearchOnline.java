
public class WordSearchOnline implements Runnable
{
	String Word = null;
	Word w = null;
	public WordSearchOnline(String Word)
	{
		this.Word = Word;
	}
	@Override
	public void run()
	{
		words.NowThreadNum++;
		w = words.SearchWordOnline(Word);
		words.WordsMap.put(Word, w);
		words.NowThreadNum--;
	}
}

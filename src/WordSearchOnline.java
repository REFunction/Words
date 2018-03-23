
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
		//System.out.println(Word + " start");
		words.NowThreadNum.getAndIncrement();
		w = words.SearchWordOnline(Word);
		words.WordsMap.put(Word, w);
		words.NowThreadNum.getAndDecrement();
		//System.out.println(Word + " end");
	}
}

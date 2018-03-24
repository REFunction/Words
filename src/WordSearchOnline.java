
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
		words.NowThreadNum.getAndIncrement();
		w = words.SearchWordOnline(Word);
		if(w == null)
		{
			words.TotalNum.decrementAndGet();
			GUI.ProcessBar.setMaximum(words.TotalNum.get());
		}
		else
			words.WordsMap.put(Word, w);
		words.NowThreadNum.getAndDecrement();
	}
}

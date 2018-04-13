import java.util.concurrent.atomic.AtomicInteger;

public class WordSearchOnline implements Runnable
{
	String Word = null;
	Word w = null;
	AtomicInteger FileSize = new AtomicInteger(0);
	public WordSearchOnline(String Word,AtomicInteger FileSize)
	{
		this.Word = Word;
		this.FileSize = FileSize;
	}
	@Override
	public void run()
	{
		words.NowThreadNum.getAndIncrement();
		w = words.SearchWordOnline(Word);
		if(!words.WordsMap.containsKey(Word))
		{
			if(w == null)
			{
				words.TotalNum.decrementAndGet();
				FileSize.decrementAndGet();
				GUI.ProcessBar.setMaximum(FileSize.get());
			}
			else
			{
				words.WordsMap.put(Word, w);
			}
		}
		
		words.NowThreadNum.getAndDecrement();
	}
}

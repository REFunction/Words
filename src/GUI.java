import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.*;

public class GUI extends JFrame implements DropTargetListener
{
	Container container = null;
	static JPanel MainPanel = null;
	static JTextField WordField = null;
	static JTextField SearchField = null;
	static JTextField PronounceField = null;
	static JTextArea ChineseArea = null;
	static JTextArea SearchResult = null;
	static String NowWord = "Drag your files";
	static JProgressBar ProcessBar = null;
	static JButton YesBtn = null;
	static JButton NoBtn = null;

	public GUI()
	{
		this.container = this.getContentPane();
		this.container.setLayout(null);
		this.setBounds(400, 130, 500, 400);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		MainPanel = new JPanel();
		MainPanel.setLayout(null);
		MainPanel.setBounds(0, 0, 500, 400);
		new DropTarget(this,DnDConstants.ACTION_COPY_OR_MOVE, this, true); 
		this.setTitle("Words");
		this.container.add(MainPanel);
		AddShowWord();
		AddProcess();
		this.setVisible(true);
		AddChinese();
		AddPronounce();
		AddYesNo();
		this.repaint();
	}

	public static void AddSearchField()
	{
		SearchField = new JTextField();
		SearchField.setBounds(50, 1, 400, 24);
		SearchField.setBackground(Color.WHITE);
		SearchField.setRequestFocusEnabled(false);
		SearchField.setHorizontalAlignment(JTextField.CENTER);
		SearchField.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				int keycode = e.getKeyCode();
				if (keycode == KeyEvent.VK_ENTER && !SearchField.getText().isEmpty())
				{
					ShowSearchResult(SearchField.getText());
				}
			}
		});
		Font f = new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 20);
		SearchField.setFont(f);
		MainPanel.add(SearchField);
	}

	private static void ShowSearchResult(String word)
	{
		String result = words.SearchWord(word);
		int LineNum = result.split("\n").length;
		if (LineNum <= 0)
			LineNum = 1;
		JDialog dialog = new JDialog();
		dialog.setBounds(400, 300, 400, 30 + LineNum * 30);
		dialog.setLayout(null);
		SearchResult = new JTextArea();
		SearchResult.setBounds(0, 0, 400, 30 + LineNum * 30);
		SearchResult.setFocusable(false);
		SearchResult.setBackground(Color.WHITE);
		SearchResult.setBorder(null);
		SearchResult.setLineWrap(true);
		SearchResult.setText(result);
		Font f = new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 20);
		SearchResult.setFont(f);
		dialog.add(SearchResult, new Integer(Integer.MAX_VALUE));
		dialog.setVisible(true);
	}

	public void AddShowWord()
	{
		WordField = new JTextField(NowWord);
		WordField.setBounds(50, 30, 400, 70);
		WordField.setFocusable(false);
		WordField.setBackground(null);
		WordField.setHorizontalAlignment(JTextField.CENTER);
		WordField.setBorder(null);
		Font f = new Font("Î¢ÈíÑÅºÚ", Font.BOLD, 40);
		WordField.setFont(f);
		MainPanel.add(WordField);
	}

	public void AddChinese()
	{
		ChineseArea = new JTextArea("");
		ChineseArea.setBounds(130, 130, 330, 100);
		ChineseArea.setFocusable(false);
		ChineseArea.setBackground(null);
		ChineseArea.setBorder(null);
		ChineseArea.setLineWrap(true);
		Font f = new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 20);
		ChineseArea.setFont(f);
		MainPanel.add(ChineseArea);
	}

	public void AddPronounce()
	{
		PronounceField = new JTextField();
		PronounceField.setBounds(50, 100, 400, 30);
		PronounceField.setFocusable(false);
		PronounceField.setBackground(null);
		PronounceField.setHorizontalAlignment(JTextField.CENTER);
		PronounceField.setBorder(null);
		Font f = new Font("Î¢ÈíÑÅºÚ", Font.BOLD, 20);
		PronounceField.setFont(f);
		MainPanel.add(PronounceField);
	}

	private void AddProcess()
	{
		ProcessBar = new JProgressBar();
		ProcessBar.setBounds(-1, 361, 500, 10);
		ProcessBar.setBorder(null);
		ProcessBar.setMaximum(words.TotalNum.get());
		ProcessBar.setForeground(Color.GREEN);
		MainPanel.add(ProcessBar);
	}

	public static void SetProcess(int FinishedNum)
	{
		ProcessBar.setValue(FinishedNum);
	}

	public static void ShowChinese(String word)
	{
		if (CalActLineNum(word) > 3)
			ChineseArea.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 13));
		else
			ChineseArea.setFont(new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, 16));
		ChineseArea.setText(words.GetChinese(word));
		PronounceField.setText(words.GetPronounce(NowWord));
	}

	private static int CalActLineNum(String s)
	{
		int LineNum = 0;
		String[] ss = s.split("\n");
		LineNum = ss.length;
		for (int i = 0; i < ss.length; i++)
			if (ss[i].length() > 10)
				LineNum++;
		return LineNum;
	}

	public static void RemoveChinese()
	{
		ChineseArea.setText("");
		PronounceField.setText("");
	}

	public void AddYesNo()
	{
		YesBtn = new JButton("Know");
		NoBtn = new JButton("Don't Know");
		YesBtn.setBounds(100, 250, 300, 50);
		YesBtn.setFocusable(false);
		YesBtn.setEnabled(false);
		Font f = new Font("Î¢ÈíÑÅºÚ", Font.BOLD, 14);
		YesBtn.setFont(f);
		MainPanel.add(YesBtn);
		YesBtn.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent arg0)
			{
				words.Yes(NowWord);
				NoBtn.setEnabled(true);
				YesBtn.setEnabled(false);
				ShowChinese(NowWord);
				words.Speak(NowWord);
			}
		});
		NoBtn.setBounds(100, 299, 300, 50);
		NoBtn.setFocusable(false);
		NoBtn.setFont(f);
		NoBtn.setEnabled(false);
		MainPanel.add(NoBtn);
		NoBtn.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent arg0)
			{
				words.No(NowWord);
				NoBtn.setEnabled(false);
				ShowChinese(NowWord);
				words.Speak(NowWord);
				if (ChineseArea.getText().isEmpty() == false)
				{
					words.No(NowWord);
					YesBtn.setEnabled(false);
				}
			}
		});
		JLabel NextLabel = new JLabel("->");
		NextLabel.setBounds(450, 0, 70, 50);
		NextLabel.setFocusable(false);
		f = new Font("Î¢ÈíÑÅºÚ", Font.BOLD, 20);
		NextLabel.setFont(f);
		NextLabel.setBorder(null);

		MainPanel.add(NextLabel);
		NextLabel.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent arg0)
			{
				if (YesBtn.isEnabled() && NoBtn.isEnabled())
					return;
				if ((!YesBtn.isEnabled()) && NoBtn.isEnabled() && words.WordsMap.get(NowWord).RemainingNum == 0)
					words.Delete(NowWord);
				YesBtn.setEnabled(true);
				NoBtn.setEnabled(true);
				String NextWord = words.GetNextWord(NowWord);
				if (NextWord.equals("All Finished"))
				{
					YesBtn.setEnabled(false);
					NoBtn.setEnabled(false);
				}
				NowWord = NextWord;
				WordField.setText(NextWord);
				RemoveChinese();
			}
		});
		this.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (NowWord.equals("All Finished"))
					return;
				int keycode = e.getKeyCode();
				if (keycode == KeyEvent.VK_1 || keycode == KeyEvent.VK_UP && YesBtn.isEnabled())
				{
					words.Yes(NowWord);
					NoBtn.setEnabled(true);
					YesBtn.setEnabled(false);
					ShowChinese(NowWord);
					words.Speak(NowWord);
				} else if (keycode == KeyEvent.VK_2 || keycode == KeyEvent.VK_DOWN && NoBtn.isEnabled())
				{
					words.No(NowWord);
					NoBtn.setEnabled(false);
					ShowChinese(NowWord);
					words.Speak(NowWord);
					if (ChineseArea.getText().isEmpty() == false)
					{
						words.No(NowWord);
						YesBtn.setEnabled(false);
					}
				} else if (keycode == KeyEvent.VK_RIGHT && NextLabel.isEnabled())
				{
					if (YesBtn.isEnabled() && NoBtn.isEnabled())
						return;
					if ((!YesBtn.isEnabled()) && NoBtn.isEnabled() && words.WordsMap.get(NowWord).RemainingNum <= 0)
						words.Delete(NowWord);
					YesBtn.setEnabled(true);
					NoBtn.setEnabled(true);
					String NextWord = words.GetNextWord(NowWord);
					if (NextWord.equals("All Finished"))
					{
						YesBtn.setEnabled(false);
						NoBtn.setEnabled(false);
						container.remove(NextLabel);
					}
					NowWord = NextWord;
					WordField.setText(NextWord);
					RemoveChinese();
				} else if (keycode == KeyEvent.VK_SPACE)
					words.Speak(NowWord);
			}
		});

	}

	public static void SetFirst()
	{
		NowWord = words.GetNextWord("");
		WordField.setText(NowWord);
		ProcessBar.setMaximum(words.TotalNum.get());
		ProcessBar.setStringPainted(true);
		ProcessBar.setValue(0);
		YesBtn.setEnabled(true);
		NoBtn.setEnabled(true);
	}

	public void dragEnter(DropTargetDragEvent dtde)
	{
		DataFlavor[] dataFlavors = dtde.getCurrentDataFlavors();
		if (dataFlavors[0].match(DataFlavor.javaFileListFlavor))
		{
			try
			{
				Transferable tr = dtde.getTransferable();
				Object obj = tr.getTransferData(DataFlavor.javaFileListFlavor);
				List<File> files = (List<File>) obj;
				YesBtn.setEnabled(false);
				NoBtn.setEnabled(false);
				
				if(words.IsReading.get())
				{
					System.out.println("111");
					return;
				}
					
				words.IsReading.set(true);
				for (int i = 0; i < files.size(); i++)
				{
					if(!words.WordFiles.contains(files.get(i)))
					{
						words.WordFiles.add(files.get(i));
					}
						
				}
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						words.ReadFromFiles();
					}
				}).start();
			} catch (UnsupportedFlavorException ex)
			{

			} catch (IOException ex)
			{

			}
		}
	}

	public void dragOver(DropTargetDragEvent dtde)
	{

	}

	public void dropActionChanged(DropTargetDragEvent dtde)
	{

	}

	public void dragExit(DropTargetEvent dte)
	{

	}

	public void drop(DropTargetDropEvent dtde)
	{

	}
}

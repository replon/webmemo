package replon.org.webmemo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebmemoController {
	
	@RequestMapping("/")
	public String main(){
		return "main";
	}
	

	@RequestMapping(value="/dir", method=RequestMethod.GET)
	public @ResponseBody String[] onGetDir(){
		
		String[] files = null;
		File dataDir = new File("data");
		if(dataDir.exists()){
			if(dataDir.isDirectory()){
				files = dataDir.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						if(new File(dir.getPath()+"/"+name).isDirectory())					
							return false;
						else
							return true;
					}
				});
			}
		}
		return files;
	}

	@RequestMapping(value="/file", method=RequestMethod.GET)
	public @ResponseBody String onGet(@RequestParam(value="title") String title){
		
		if(title.contains("..") || title.contains("*") )
			return "WRONG PATH OR FILENAME!";
		
		String result = "";
		try(FileInputStream fis = new FileInputStream("data/"+title);
			BufferedInputStream bis = new BufferedInputStream(fis);
			Scanner sc = new Scanner(bis);){
			
			StringBuffer sb = new StringBuffer();
			while(sc.hasNextLine()){
				sb.append(sc.nextLine()+"\n");
			}
			result = sb.toString();
			
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value="/file", method=RequestMethod.POST)
	public @ResponseBody String onPost(
			@RequestParam(value="text") String text,
			@RequestParam(value="title") String title
			) {
		System.out.println("["+title+"] "+text);
		
		if(text.length()>2000000)
			return "error(tooBig)";
		
		if(title.contains("..") || title.contains("*") )
			return "error(wrongFileName)";
		
		try {
			
			File dataDir = new File("data");
			if(dataDir.exists()){
				if(dataDir.isDirectory())
					;
				else
					throw new IOException("file named 'data' must be not exist!");
			} else{
				dataDir.mkdir();
			}
			
			File file = new File("data/"+title);
			
			if(!file.getParent().equals("data")){
				file.getParentFile().mkdirs();
			}
			
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(text.getBytes());
			
			bos.close();
			fos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return "error(IOException)";
		}
		
		
        return "ok";
    }
}

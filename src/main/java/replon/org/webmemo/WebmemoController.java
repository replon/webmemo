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

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Controller
public class WebmemoController {
	
	@RequestMapping("/")
	public String main(){

		System.out.println("ACCESS");
		printIP();
		
		return "main";
	}
	

	/**
	 * Load Public Files
	 * @return
	 */
	@RequestMapping(value="/publics", method=RequestMethod.GET)
	public @ResponseBody String[] onGetPublics(){
		
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

	@RequestMapping(value="/del", method=RequestMethod.POST)
	public @ResponseBody String onDel(@RequestParam(value="title") String title){

		System.out.println("DELETE : ["+title+"]");
		printIP();
		
		if(title.contains("..") || title.contains("*") )
			return "DON'T TEST ME!";
		
		try(FileInputStream fis = new FileInputStream("data/"+title);
			BufferedInputStream bis = new BufferedInputStream(fis);
			Scanner sc = new Scanner(bis);){
			
			
			File file = new File("del/"+title);
			
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			while(sc.hasNextLine()){
				bos.write(sc.nextLine().getBytes());
			}
			
			bos.close();
			fos.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "error";
		} catch (IOException e) {
			e.printStackTrace();
			return "error";
		}

		try{
			File toDel = new File("data/"+title);
			toDel.delete();
		} catch (Exception e){
			e.printStackTrace();
			return "error";
			
		}
		
		return "ok";
	}

	/**
	 * Read a file
	 * @param title
	 * @return
	 */
	@RequestMapping(value="/file", method=RequestMethod.GET)
	public @ResponseBody String onGet(@RequestParam(value="title") String title){

		System.out.println("READ : ["+title+"]");
		printIP();
		
		if(title.contains("..") || title.contains("*") )
			return "DON'T TEST ME!";
		
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
	
	/**
	 * Write a File
	 * @param text
	 * @param title
	 * @return
	 */
	@RequestMapping(value="/file", method=RequestMethod.POST)
	public @ResponseBody String onPost(
			@RequestParam(value="text") String text,
			@RequestParam(value="title") String title
			) {
		
		System.out.println("WRITE : ["+title+"]\n"+text);
		printIP();
		
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

	private static final String[] HEADERS_TO_TRY = { 
	    "X-Forwarded-For",
	    "Proxy-Client-IP",
	    "WL-Proxy-Client-IP",
	    "HTTP_X_FORWARDED_FOR",
	    "HTTP_X_FORWARDED",
	    "HTTP_X_CLUSTER_CLIENT_IP",
	    "HTTP_CLIENT_IP",
	    "HTTP_FORWARDED_FOR",
	    "HTTP_FORWARDED",
	    "HTTP_VIA",
	    "REMOTE_ADDR" };
	
	private void printIP() {

		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String ip = null;
	    for (String header : HEADERS_TO_TRY) {
	        ip = request.getHeader(header);
	        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
	            System.out.println("IP : "+ip);
	        }
	    }
        System.out.println("IP : "+request.getRemoteAddr());
	}
}

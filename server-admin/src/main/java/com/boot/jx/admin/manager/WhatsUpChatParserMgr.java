package com.boot.jx.admin.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.boot.jx.admin.dto.ChatParserDto;
import com.boot.utils.ArgUtil;
@Component
public class WhatsUpChatParserMgr {
	
	public List<ChatParserDto> chatParser()  {
		List<ChatParserDto> chatLst=new ArrayList<ChatParserDto>();
		    	// TODO Auto-generated method stub
		        char preString = '-';
		        char searchString = ':';
		    try {
		        //FileReader fr = new FileReader("E:\\whatsup_chat\\chat.txt");
		        FileReader fr = new FileReader("E:\\whatsup_chat\\chat_with_m.txt");
		        BufferedReader br = new BufferedReader(fr);

		   

		        String line = "";
		        String buffer = "";
		        String lastMember = null;
		        while ((line = br.readLine()) != null) {
		            System.out.println("\n" + line);
		            
		            if (!line.isEmpty()) {
		            	ChatParserDto dto=new ChatParserDto();
		            	String str[]=line.split("-");
		            	if(str!=null && str.length>=2) {
		            		String date=str[0];
		            		dto.setDate(date);
		            	
		            		String strMore=str[1];
		            		if(strMore!=null) {
		            			String strMoreAr[]=strMore.split(":");
		            			if(strMoreAr!=null && strMoreAr.length>=2) {
		            				String auther=strMoreAr[0];
		            				String msg=strMoreAr[1];
		            				dto.setAuther(auther);
		            				dto.setMessage(msg);
		            				
		            				
		            			}
		            		}
		            		
		            	}

		            if(dto!=null && ArgUtil.is(dto.getDate())){
		            	chatLst.add(dto);
		            }
		            }
		           
		        }
		    }catch (Exception e) {
				e.printStackTrace();
			}finally {
				
			}
		return chatLst;
	}
	
	 
    public List<ChatParserDto> getParseFileUsingRegExp() {
        //a. I would reference to my file
       // File wspLogFile = new File("data/wsp.log");
        //b. I would use the mechanism to read the file using BufferedReader
        //BufferedReader bufferedReader = new BufferedReader(new FileReader(wspLogFile));
    	
    	List<ChatParserDto> chatLst=new ArrayList<ChatParserDto>();
    	try {
    	
    	 FileReader fr = new FileReader("E:\\whatsup_chat\\mehery_chat.txt");
         BufferedReader bufferedReader = new BufferedReader(fr);

        String currLine = null;//This is the current line (like my cursor)

        //This will hold the data of the file in String format
        StringBuilder stringFormatter = new StringBuilder();
        boolean firstIterationDone = false;//The first line will always contains the format, so I will always append it, from the second I will start making the checkings...

        String regex = "(\\d+/\\d+/\\d+, \\d+:\\d+\\d+ [A-Z]*) - (.*?): (.*)";
        
        String sCurrentLine;
        
        Pattern r = Pattern.compile(regex); //REGEX required for extracting data
        while ((sCurrentLine = bufferedReader.readLine()) != null) {
        System.out.println(sCurrentLine);
        Matcher m = r.matcher(sCurrentLine);

        if (m.find()) {
        	ChatParserDto dto=new ChatParserDto();
          String date=m.group(1);
          String auther=m.group(2);
		  String msg=m.group(3);
          dto.setDate(date);
          dto.setAuther(auther);
		  dto.setMessage(msg);
          
         // System.out.println("Message: " + m.group(4) );
          if(dto!=null && ArgUtil.is(dto.getDate())){
          	chatLst.add(dto);
          }
        }
        }
        }catch (Exception e) {
			e.printStackTrace();
		}finally {
			
		}
        
        return chatLst;   
      
    }
	
}

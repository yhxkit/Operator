package com.sample.operator.app.ctrl.chat;


import com.sample.operator.app.common.exception.OperException;
import com.sample.operator.app.common.util.ResponseMaker;
import com.sample.operator.app.svc.fileBiz.ChatLogSvc;
import com.sample.operator.app.svc.fileBiz.ServerFileSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ChatRestCtrl {

    private final ResponseMaker responseMaker;
    private final ServerFileSvc serverFileSvc;
    private final ChatLogSvc chatLogSvc;

    @GetMapping("/chat/download/{fileName}")
    public ResponseEntity<?> getFileFromServer(@PathVariable("fileName") String fileName)
    {
        try{
            byte[] data = serverFileSvc.getFile(fileName);
            return responseMaker.makeDownloadableResource(fileName, data);
        }
        catch (OperException e)
        {
            System.out.println("파일 저장 오류" +OperException.getStackTrace(e));
            return ResponseEntity.internalServerError().body(e.getExMsg());
        }
        catch (Exception e)
        {
            System.out.println("파일 저장 오류 " + OperException.getStackTrace(e));
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/chat/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file)
    {
        try{
            String fileName = file.getOriginalFilename();
            byte[] bt = file.getBytes();
            return ResponseEntity.ok(serverFileSvc.saveFile(fileName, bt));
        } catch (Exception e) {
            System.out.println("파일 저장 오류" + OperException.getStackTrace(e));
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/chat/log")
    public ResponseEntity<?> getLog()
    {
        return ResponseEntity.ok(chatLogSvc.getTodayChat());
    }
}

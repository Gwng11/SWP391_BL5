package com.hotel.controller;

import com.hotel.service.ManagerService;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** UC62 - URL gallery operations and local JPG/PNG/WEBP upload. */
@WebServlet(urlPatterns={"/manager/room-types/images"})
@MultipartConfig(fileSizeThreshold=256*1024,maxFileSize=5*1024*1024,maxRequestSize=6*1024*1024)
public class RoomImageController extends BaseController{
    private static final Map<String,String> EXTENSIONS=Map.of("image/jpeg",".jpg","image/png",".png","image/webp",".webp");
    private final ManagerService service=new ManagerService();

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        long typeId;
        try{typeId=longParam(req,"roomTypeId");}catch(RuntimeException e){redirect(req,resp,"/manager/room-types","err","Thiếu loại phòng");return;}
        try{
            String action=req.getParameter("action");
            if("upload".equals(action))upload(req,typeId);
            else if("addUrl".equals(action))service.addImage(typeId,req.getParameter("imageUrl"));
            else if("updateUrl".equals(action))service.updateImage(typeId,intParam(req,"index",-1),req.getParameter("imageUrl"));
            else if("moveUp".equals(action))service.moveImage(typeId,intParam(req,"index",-1),-1);
            else if("moveDown".equals(action))service.moveImage(typeId,intParam(req,"index",-1),1);
            else if("remove".equals(action))remove(req,typeId,intParam(req,"index",-1));
            else throw new IllegalArgumentException("Thao tác ảnh không hợp lệ");
            redirect(req,resp,"/manager/room-types?edit="+typeId,"msg","Đã cập nhật thư viện ảnh");
        }catch(IllegalArgumentException|IllegalStateException e){redirect(req,resp,"/manager/room-types?edit="+typeId,"err",e.getMessage());}
        catch(Exception e){redirect(req,resp,"/manager/room-types?edit="+typeId,"err","Không thể xử lý ảnh. Kiểm tra dung lượng/định dạng và thử lại.");}
    }

    private void upload(HttpServletRequest req,long typeId)throws Exception{
        Part part=req.getPart("imageFile");if(part==null||part.getSize()==0)throw new IllegalArgumentException("Vui lòng chọn ảnh");
        String extension=EXTENSIONS.get(part.getContentType());if(extension==null)throw new IllegalArgumentException("Chỉ hỗ trợ JPG, PNG hoặc WEBP");
        String real=req.getServletContext().getRealPath("/uploads/room-types");if(real==null)throw new IllegalStateException("Deployment hiện tại không hỗ trợ lưu upload cục bộ");
        Path dir=Path.of(real).toAbsolutePath().normalize();Files.createDirectories(dir);Path file=dir.resolve(UUID.randomUUID()+extension).normalize();if(!file.startsWith(dir))throw new IllegalStateException("Đường dẫn upload không an toàn");
        try(var input=new BufferedInputStream(part.getInputStream())){
            input.mark(16);
            byte[] signature=input.readNBytes(12);
            input.reset();
            if(!hasExpectedSignature(signature,part.getContentType()))
                throw new IllegalArgumentException("Nội dung file không đúng định dạng ảnh đã chọn");
            Files.copy(input,file,StandardCopyOption.REPLACE_EXISTING);
        }
        String webPath="/uploads/room-types/"+file.getFileName();
        try{service.addImage(typeId,webPath);}catch(RuntimeException e){Files.deleteIfExists(file);throw e;}
    }

    private void remove(HttpServletRequest req,long typeId,int index)throws IOException{
        List<String> images=service.getImages(typeId);if(index<0||index>=images.size())throw new IllegalArgumentException("Vị trí ảnh không hợp lệ");String value=images.get(index);Path original=localPath(req,value);Path pending=null;
        if(original!=null&&Files.exists(original)){pending=original.resolveSibling(original.getFileName()+".pending-"+UUID.randomUUID());Files.move(original,pending);}
        try{service.removeImage(typeId,index);if(pending!=null)Files.deleteIfExists(pending);}catch(RuntimeException|IOException e){if(pending!=null&&Files.exists(pending))Files.move(pending,original,StandardCopyOption.REPLACE_EXISTING);throw e;}
    }

    private Path localPath(HttpServletRequest req,String value){
        if(value==null||!value.startsWith("/uploads/room-types/"))return null;String root=req.getServletContext().getRealPath("/uploads/room-types");if(root==null)return null;Path dir=Path.of(root).toAbsolutePath().normalize();Path result=dir.resolve(value.substring(value.lastIndexOf('/')+1)).normalize();return result.startsWith(dir)?result:null;
    }

    static boolean hasExpectedSignature(byte[] value,String contentType){
        if(value==null)return false;
        if("image/jpeg".equals(contentType))return value.length>=3&&(value[0]&255)==0xFF&&(value[1]&255)==0xD8&&(value[2]&255)==0xFF;
        if("image/png".equals(contentType))return value.length>=8&&(value[0]&255)==0x89&&value[1]==0x50&&value[2]==0x4E&&value[3]==0x47&&value[4]==0x0D&&value[5]==0x0A&&value[6]==0x1A&&value[7]==0x0A;
        if("image/webp".equals(contentType))return value.length>=12&&value[0]=='R'&&value[1]=='I'&&value[2]=='F'&&value[3]=='F'&&value[8]=='W'&&value[9]=='E'&&value[10]=='B'&&value[11]=='P';
        return false;
    }
}

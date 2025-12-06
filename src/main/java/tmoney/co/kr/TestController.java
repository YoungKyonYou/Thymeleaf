package tmoney.co.kr;


import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tmoney.co.kr.imports.ImportDemoRequest;

import java.util.List;


@RequiredArgsConstructor
@Controller
public class TestController {
  //  private final UserService userService;
    @GetMapping({"/", "/index"})
    public String home() {
        return "page/dashboard/index";
    }

    @GetMapping("/page/dashboard/index")
    public String dashboard() {
        return "page/dashboard/index";
    }

    @GetMapping("/common/import/demo")
    public String importDemo() {


        return "common/imports/import-demo";
    }


    @GetMapping("/test/multipart-form.do")
    public String multipartForm() {
        return "test/multipartForm";
    }

    @PostMapping(
            value = "/test/multipart-echo.do",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Map<String, Object> echo(
            @RequestPart("file") MultipartFile file,
            @RequestPart("data") MultipartEchoReq data
    ) {
        Map<String, Object> out = new LinkedHashMap<>();

        Map<String, Object> fileInfo = new LinkedHashMap<>();
        fileInfo.put("originalFilename", file != null ? file.getOriginalFilename() : null);
        fileInfo.put("contentType", file != null ? file.getContentType() : null);
        fileInfo.put("size", file != null ? file.getSize() : 0);

        out.put("receivedData", data);
        out.put("receivedFile", fileInfo);
        out.put("success", true);

        return out;
    }


    @PostMapping("/api/users/import")
    public ResponseEntity<Void> importDemo(@RequestBody List<ImportDemoRequest> list) {

        System.out.println("받은 payload = " + list);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/page/guide/{view}")
    public String guide(@PathVariable String view, Model model) {
        return "page/guide/" + view;
    }

//    @GetMapping("/page/guide/{view}")//    public String guide(@PathVariable String view,@ModelAttribute UserSearchRequest req2, Model model) {

    /// ///        UserSearchRequest req =  new UserSearchRequest();
    /// ///        PageData<UserDto> contents = userService.search(req, req2 == null ? 1 : req2.getPage(), 10, "id", "asc");
    /// ///
    /// ///        model.addAttribute("pageData", contents);
    /// /
    /// /        return "page/guide/" + view;
    /// /    }


    @GetMapping("/page/menu1/{view}")
    public String menu1(@PathVariable String view) {
        return "page/menu1/" + view;
    }

    @GetMapping("/component/{view}")
    public String component(@PathVariable String view) {
        return "component/" + view;
    }

    /* ─────────────────────────────────────────────────────────────
       [옵션] “리다이렉트만 하면 된다” 요구 대응용: /go/** 로 들어오면 동일 경로로 리다이렉트
       예) /go/page/guide/page3  -> redirect:/page/guide/page3
       ───────────────────────────────────────────────────────────── */
    @GetMapping("/go/page/guide/{view}")
    public String goGuide(@PathVariable String view) {
        return "redirect:/page/guide/" + view;
    }

    @GetMapping("/go/page/dashboard/index")
    public String goDashboard() {
        return "redirect:/page/dashboard/index";
    }

    @GetMapping("/go/menu1/{view}")
    public String goMenu1(@PathVariable String view) {
        return "redirect:/menu1/" + view;
    }

    @GetMapping("/go/component/{view}")
    public String goComponent(@PathVariable String view) {
        return "redirect:/component/" + view;
    }
}

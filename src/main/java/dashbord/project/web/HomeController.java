package dashbord.project.web;

import dashbord.project.DashboardMaker;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.RegEx;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static java.net.URLEncoder.encode;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping("/")
    public String index(Model model,
                        @PathVariable(required = false) String error,
                        @PathVariable(required = false) String error_message) {

        model.addAttribute("error", error);
        model.addAttribute("error_message", error_message);
        return "home_page";
    }

    @PostMapping("/post")
    public String makeArchive(
                              @RequestParam(required = false) String sparqlService,
                              @RequestParam String queryString,
                              @RequestParam String chartType,
                              @RequestParam String name,
                              @RequestParam(required = false) String columID) {
        if (queryString.isBlank() || queryString.isBlank()) {
            return "redirect:/?error=true&error_message=" + "Sparql+query+string+is+empty";
        }
        if(name.isBlank() || name.isEmpty()){
            return "redirect:/?error=true&error_message=" + "Write+the+name+please";
        }

        ChartType type = ChartType.valueOf(chartType);

        boolean succ;
        DashboardMaker dashboardMaker = new DashboardMaker();

        try {
            succ = dashboardMaker.createDashboard(sparqlService, queryString, type, name, columID);
        } catch (Exception e){
            String message = encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/?error=true&error_message=" + message.replaceAll("\s","+");
        }
        if (succ){
            return "redirect:/?succ=true&message=Successful";
        }

        return "redirect:/?error=true&error_message=Something+went_wrong";
    }
}

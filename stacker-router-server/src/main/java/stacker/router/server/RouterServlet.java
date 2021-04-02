package stacker.router.server;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = true,
        description = "Router servlet reference implementation"
)
public class RouterServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) {

    }

}

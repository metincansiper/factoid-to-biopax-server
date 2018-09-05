package web;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import converter.FactoidToBiopax;

/**
 * Servlet implementation class ConvertToOwl
 */
@WebServlet("/ConvertToOwl")
public class ConvertToOwlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConvertToOwlServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Create input stream reader by the request
		InputStreamReader reader = new InputStreamReader(request.getInputStream());
		
		// Add templates to converter by the reader
		FactoidToBiopax converter = new FactoidToBiopax();
		converter.addToModel(reader);
		
		// Convert the model to biopax string
		String biopaxStr = converter.convertToOwl();
		
		// Append the result to the writer
		response.getWriter().append(biopaxStr);
	}

}

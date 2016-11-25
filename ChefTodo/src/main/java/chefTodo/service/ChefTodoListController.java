package chefTodo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import chefTodo.readModel.ChefTodoListQueries;
import chefTodo.readModel.TodoListGroup;

@Controller
public class ChefTodoListController {
	@Autowired
	@Qualifier("chefTodo")
	private ChefTodoListQueries chefTodo;

	@RequestMapping(value = "/chef", method = RequestMethod.GET)
	public @ResponseBody List<TodoListGroup> getTodo() {
		return chefTodo.getTodoList();
	}
}

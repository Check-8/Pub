package chefTodo.service;

import chefTodo.readModel.ChefTodoListQueries;
import chefTodo.readModel.TodoListGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChefTodoListController {
	@Autowired
	@Qualifier("chefTodo")
	private ChefTodoListQueries chefTodo;

	@GetMapping(value = "/chef")
	public @ResponseBody List<TodoListGroup> getTodo() {
		return chefTodo.getTodoList();
	}
}

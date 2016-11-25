Lo script `mvn_package` effettua il package per ogni progetto.
Lo script `build_all_services` avvia `docker-compose build`.
Lo script `start_all_services` avvia `docker-compose up -d`.
Lo script `kill_all_services` avvia `docker-compose kill`.

L'homepage è raggiungibile su `localhost:8080/ui/`.

Ogni servizio espone le proprie operazioni tramite operazioni REST.
Esempi scritti per curl:
- Tab: write side delle operazioni. Accetta operazioni per:
	* aprire un nuovo conto: curl -H "Content-Type: application/json" -X POST -d '{"id":0, "table_number":0, "waiter":"Jack"}' localhost:8080/tab/opentab/
	* creare un ordine: curl -H "Content-Type: application/json" -X POST -d '{"id":0, "ordered_item[]":[1,1,1,1,10,5]}' localhost:8080/tab/placeorder/
	* segnare drink serviti: curl -H "Content-Type: application/json" -X POST -d '{"id":0, "drinks_served[]":[1,1]}' localhost:8080/tab/markdrinksserved/
	* segnare cibi preparati: curl -H "Content-Type: application/json" -X POST -d '{"id":0, "food_prepared[]":[10,5]}' localhost:8080/tab/markfoodprepared/
	* segnare cibi serviti: curl -H "Content-Type: application/json" -X POST -d '{"id":0, "food_served[]":[5]}' localhost:8080/tab/markfoodserved/
	* chiudere un conto: curl -H "Content-Type: application/json" -X POST -d '{"id":0, "amount_paid":500.0}' localhost:8080/tab/closetab/
- OpenTab: read side per conti aperti, informazioni su prodotti ancora da servire:
	* ottenere informazioni da ID: curl -G localhost:8080/opentab/tab/id/0
	* ottenere informazioni tramite numero tavolo: curl -G localhost:8080/opentab/tab/table/0
	* ottenere elenco prodotti da servire raggruppati per numero tavolo per un cameriere: curl -G localhost:8080/opentab/tab/Jack
	* ottenere conto attuale da pagare tramite numero tavolo: curl -G localhost:8080/opentab/tab/topay/0
- ChefTodo: read side per cibi da preparare:
	* ottenere lista dei cibi da servire, raggruppati per ordine: curl -G localhost:8080/cheftodo/chef/
- Menu: miniservizio per leggere informazioni sul menu, informazioni salvate su file
	* informazioni su un singolo prodotto: curl -G localhost:8080/menu/1
	* intero menu: curl -G localhost:8080/menu/menu
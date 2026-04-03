[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/e4rOHRfR)

## Mia Ark (pzc4aq)
### GitHub Username - @miaark

### APP DETAILS + FUNCTIONALITY
- View placemarks at UVA based on their place tags
- After selecting a tag from the alphabetically-sorted, scrollable dropdown, the map below populates with all places on grounds that match that place type
- Map placemarks show the place's name/title and a snippet of a description of that place; clicking on that placemark allows you to see the full description for that place
- On start-up, the app pulls from the placemarks API (https://www.cs.virginia.edu/~wxt4gm/placemarks.json), storing any necessary data in the SQLite database. Data is only pulled from the API on start-up, and the rest of your app uses data from the SQLite database. Subsequent runs should only synchronize the data, not re-add it

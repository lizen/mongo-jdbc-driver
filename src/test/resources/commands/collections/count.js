// before
db.col.insert({name: "value1", v: 1});
db.col.insert({name: "value2", v: 2});
db.col.insert({name: "value2", v: 3});
// command
db.col.count();
// clear
db.getCollection('col').drop();
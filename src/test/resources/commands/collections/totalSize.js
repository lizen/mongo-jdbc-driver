// before
db.col.insert({key: "value"});
// command
db.col.totalSize();
// clear
db.getCollection('col').drop();
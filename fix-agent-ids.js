const db = connect("mongodb://localhost:27017/travelagency");

db.tours.find({}).forEach(t => {
  if (t.assignedAgentId) {
    db.tours.updateOne(
      { _id: t._id },
      { $set: { assignedAgentId: t.assignedAgentId.toString() } }
    );
  }
});

print("✅ Fixed: assignedAgentId converted to strings in all tours");

// Verify
const sample = db.tours.findOne();
print("Type check: " + typeof sample.assignedAgentId + " = " + sample.assignedAgentId);


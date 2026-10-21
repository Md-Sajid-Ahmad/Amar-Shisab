const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- Unauthenticated tests ---
test("Unauthenticated user: cannot read user documents", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot read transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
});

// --- Authenticated user tests ---
test("Authenticated user: can create and read own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create, read, update own account", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const accRef = aliceDb.collection("users").doc(ALICE_UID).collection("accounts").doc("acc_1");
  await assertSucceeds(
    accRef.set({
      userId: ALICE_UID,
      name: "Cash",
      iconEmoji: "💵",
      colorHex: "#0F2537",
      localId: 1,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(accRef.get());
});

test("Authenticated user: can create and list own transactions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txRef = aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1");
  await assertSucceeds(
    txRef.set({
      userId: ALICE_UID,
      amount: 500,
      description: "Groceries",
      category: "Food",
      type: "EXPENSE",
      notes: "Daily bazaar",
      timestamp: Date.now(),
      yearMonth: "2026-10",
      localAccountId: 1,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
});

test("Authenticated user: cannot create transaction with invalid amount", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txRef = aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_bad");
  await assertFails(
    txRef.set({
      userId: ALICE_UID,
      amount: -100, // Invalid!
      description: "Invalid amount",
      category: "Food",
      type: "EXPENSE",
      timestamp: Date.now(),
      yearMonth: "2026-10",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Cross-user isolation: Bob cannot read or write Alice's transactions", async () => {
  // Seed Alice transaction using admin context
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("transactions").doc("alice_secret").set({
      userId: ALICE_UID,
      amount: 1000,
      description: "Secret expense",
      category: "Personal",
      type: "EXPENSE",
      timestamp: Date.now(),
      yearMonth: "2026-10",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  // Bob cannot read Alice's transaction
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("alice_secret").get());
  // Bob cannot write to Alice's subcollection
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("bob_hack").set({
      userId: ALICE_UID,
      amount: 50,
      description: "Hacked",
      category: "General",
      type: "EXPENSE",
      timestamp: Date.now(),
      yearMonth: "2026-10",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

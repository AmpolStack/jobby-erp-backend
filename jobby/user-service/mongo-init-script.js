// =============================================================================
// MongoDB Initialization Script - User Service
// =============================================================================
// Executed automatically by /docker-entrypoint-initdb.d/ on first container start.
// Runs inside the database defined by MONGO_INITDB_DATABASE env variable.
// =============================================================================

print("==========================================");
print(" Initializing User Service Collections");
print("==========================================");

// ---------------------------------------------------------------------------
// 1. identification_types
// ---------------------------------------------------------------------------
print("[1/6] Creating 'identification_types' collection...");
db.createCollection("identification_types", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "dian_code", "min_length", "max_length", "name", "expression", "abbreviation", "allow_characters"],
      properties: {
        _id: {
          bsonType: "int",
          description: "Unique identifier (integer)"
        },
        dian_code: {
          bsonType: "int",
          minimum: 10,
          maximum: 99,
          description: "DIAN code for the identification type (10-99)"
        },
        min_length: {
          bsonType: "int",
          minimum: 1,
          description: "Minimum length for identification number"
        },
        max_length: {
          bsonType: "int",
          minimum: 1,
          maximum: 50,
          description: "Maximum length for identification number (1-50)"
        },
        name: {
          bsonType: "string",
          maxLength: 10,
          description: "Identification type name, max 10 characters"
        },
        expression: {
          bsonType: "string",
          maxLength: 200,
          description: "Validation expression, max 200 characters"
        },
        abbreviation: {
          bsonType: "string",
          maxLength: 5,
          description: "Abbreviation, max 5 characters"
        },
        allow_characters: {
          bsonType: "array",
          description: "Set of allowed characters (each max 15 chars)",
          items: {
            bsonType: "string",
            maxLength: 15
          }
        }
      },
      additionalProperties: false
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});
print("  ✓ 'identification_types' created.");

// ---------------------------------------------------------------------------
// 2. contact_types
// ---------------------------------------------------------------------------
print("[2/6] Creating 'contact_types' collection...");
db.createCollection("contact_types", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "type", "description", "expression"],
      properties: {
        _id: {
          bsonType: "int",
          description: "Unique identifier (integer)"
        },
        type: {
          bsonType: "string",
          maxLength: 50,
          description: "Contact type name, max 50 characters"
        },
        description: {
          bsonType: "string",
          maxLength: 150,
          description: "Contact type description, max 150 characters"
        },
        expression: {
          bsonType: "string",
          maxLength: 200,
          description: "Validation expression, max 200 characters"
        },
        visual_instructions: {
          bsonType: "object",
          description: "Key-value map of visual instructions (keys max 20 chars, values max 100 chars)",
          additionalProperties: {
            bsonType: "string",
            maxLength: 100
          }
        }
      },
      additionalProperties: false
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});
print("  ✓ 'contact_types' created.");

// ---------------------------------------------------------------------------
// 3. municipalities
// ---------------------------------------------------------------------------
print("[3/6] Creating 'municipalities' collection...");
db.createCollection("municipalities", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "department", "name"],
      properties: {
        _id: {
          bsonType: "int",
          description: "Unique identifier (integer)"
        },
        department: {
          bsonType: "object",
          required: ["_id", "name"],
          description: "Embedded department document",
          properties: {
            _id: {
              bsonType: "int",
              description: "Department identifier (integer)"
            },
            name: {
              bsonType: "string",
              maxLength: 50,
              description: "Department name, max 50 characters"
            },
            dane_code: {
              bsonType: "int",
              description: "DANE code for the department"
            }
          },
          additionalProperties: false
        },
        name: {
          bsonType: "string",
          maxLength: 50,
          description: "Municipality name, max 50 characters"
        },
        dane_code: {
          bsonType: "int",
          description: "DANE code for the municipality"
        }
      },
      additionalProperties: false
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});
print("  ✓ 'municipalities' created.");

// ---------------------------------------------------------------------------
// 4. users
// ---------------------------------------------------------------------------
print("[4/6] Creating 'users' collection...");
db.createCollection("users", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: [
        "_id",
        "first_name",
        "role",
        "identification_number",
        "identification_number_searchable",
        "email",
        "email_searchable",
        "phone",
        "phone_searchable",
        "created_at",
        "modified_at"
      ],
      properties: {
        _id: {
          bsonType: "long",
          description: "Unique identifier (long)"
        },
        contacts: {
          bsonType: "array",
          description: "Set of contact sub-documents",
          items: {
            bsonType: "object",
            required: ["_id", "contact_type_id", "name", "value"],
            properties: {
              _id: {
                bsonType: "long",
                description: "Contact identifier (long)"
              },
              contact_type_id: {
                bsonType: "int",
                description: "Reference to contact_types collection"
              },
              name: {
                bsonType: "string",
                maxLength: 50,
                description: "Contact name, max 50 characters"
              },
              description: {
                bsonType: "string",
                maxLength: 150,
                description: "Contact description, max 150 characters"
              },
              is_public: {
                bsonType: "bool",
                description: "Whether this contact is public"
              },
              value: {
                bsonType: "string",
                maxLength: 250,
                description: "Contact value, max 250 characters"
              }
            },
            additionalProperties: false
          }
        },
        identification_type_id: {
          bsonType: "int",
          description: "Reference to identification_types collection"
        },
        first_name: {
          bsonType: "string",
          maxLength: 150,
          description: "User first name, max 150 characters"
        },
        last_name: {
          bsonType: "string",
          maxLength: 150,
          description: "User last name, max 150 characters"
        },
        role: {
          bsonType: "string",
          maxLength: 10,
          description: "User role, max 10 characters"
        },
        is_active: {
          bsonType: "bool",
          description: "Whether the user is active"
        },
        profile_image_url: {
          bsonType: "string",
          description: "URL to the user's profile image"
        },
        identification_number: {
          bsonType: "binData",
          description: "Encrypted identification number"
        },
        identification_number_searchable: {
          bsonType: "binData",
          description: "Searchable (hashed) identification number"
        },
        email: {
          bsonType: "binData",
          description: "Encrypted email"
        },
        email_searchable: {
          bsonType: "binData",
          description: "Searchable (hashed) email"
        },
        phone: {
          bsonType: "string",
          description: "User phone number"
        },
        phone_searchable: {
          bsonType: "string",
          description: "Searchable phone number"
        },
        created_at: {
          bsonType: "date",
          description: "Creation timestamp"
        },
        modified_at: {
          bsonType: "date",
          description: "Last modification timestamp"
        }
      },
      additionalProperties: false
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});
print("  ✓ 'users' created.");

// ---------------------------------------------------------------------------
// 5. owners
// ---------------------------------------------------------------------------
print("[5/6] Creating 'owners' collection...");
db.createCollection("owners", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "user", "created_at", "modified_at"],
      properties: {
        _id: {
          bsonType: "long",
          description: "Unique identifier (long)"
        },
        user: {
          bsonType: "object",
          required: [
            "_id",
            "first_name",
            "role",
            "identification_number",
            "identification_number_searchable",
            "email",
            "email_searchable",
            "phone",
            "phone_searchable",
            "created_at",
            "modified_at"
          ],
          description: "Embedded user document",
          properties: {
            _id: {
              bsonType: "long",
              description: "User identifier"
            },
            contacts: {
              bsonType: "array"
            },
            identification_type_id: {
              bsonType: "int"
            },
            first_name: {
              bsonType: "string",
              maxLength: 150
            },
            last_name: {
              bsonType: "string",
              maxLength: 150
            },
            role: {
              bsonType: "string",
              maxLength: 10
            },
            is_active: {
              bsonType: "bool"
            },
            profile_image_url: {
              bsonType: "string"
            },
            identification_number: {
              bsonType: "binData"
            },
            identification_number_searchable: {
              bsonType: "binData"
            },
            email: {
              bsonType: "binData"
            },
            email_searchable: {
              bsonType: "binData"
            },
            phone: {
              bsonType: "string"
            },
            phone_searchable: {
              bsonType: "string"
            },
            created_at: {
              bsonType: "date"
            },
            modified_at: {
              bsonType: "date"
            }
          }
        },
        alternative_email: {
          bsonType: "binData",
          description: "Encrypted alternative email"
        },
        alternative_email_searchable: {
          bsonType: "binData",
          description: "Searchable (hashed) alternative email"
        },
        secureParameters: {
          bsonType: "object",
          description: "Key-value map of secure parameters (keys max 20 chars, values max 100 chars)",
          additionalProperties: {
            bsonType: "string",
            maxLength: 100
          }
        },
        created_at: {
          bsonType: "date",
          description: "Creation timestamp"
        },
        modified_at: {
          bsonType: "date",
          description: "Last modification timestamp"
        }
      },
      additionalProperties: false
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});
print("  ✓ 'owners' created.");

// ---------------------------------------------------------------------------
// 6. employees
// ---------------------------------------------------------------------------
print("[6/6] Creating 'employees' collection...");
db.createCollection("employees", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "position_name", "created_at", "modified_at"],
      properties: {
        _id: {
          bsonType: "long",
          description: "Unique identifier (long)"
        },
        user: {
          bsonType: "object",
          required: [
            "_id",
            "first_name",
            "role",
            "identification_number",
            "identification_number_searchable",
            "email",
            "email_searchable",
            "phone",
            "phone_searchable",
            "created_at",
            "modified_at"
          ],
          description: "Embedded user document",
          properties: {
            _id: {
              bsonType: "long",
              description: "User identifier"
            },
            contacts: {
              bsonType: "array"
            },
            identification_type_id: {
              bsonType: "int"
            },
            first_name: {
              bsonType: "string",
              maxLength: 150
            },
            last_name: {
              bsonType: "string",
              maxLength: 150
            },
            role: {
              bsonType: "string",
              maxLength: 10
            },
            is_active: {
              bsonType: "bool"
            },
            profile_image_url: {
              bsonType: "string"
            },
            identification_number: {
              bsonType: "binData"
            },
            identification_number_searchable: {
              bsonType: "binData"
            },
            email: {
              bsonType: "binData"
            },
            email_searchable: {
              bsonType: "binData"
            },
            phone: {
              bsonType: "string"
            },
            phone_searchable: {
              bsonType: "string"
            },
            created_at: {
              bsonType: "date"
            },
            modified_at: {
              bsonType: "date"
            }
          }
        },
        address: {
          bsonType: "object",
          description: "Embedded address sub-document",
          required: ["_id", "municipality", "direction", "created_at", "modified_at"],
          properties: {
            _id: {
              bsonType: "long",
              description: "Address identifier"
            },
            municipality: {
              bsonType: "object",
              required: ["_id", "name"],
              description: "Embedded municipality reference",
              properties: {
                _id: {
                  bsonType: "int",
                  description: "Municipality identifier"
                },
                department: {
                  bsonType: "object",
                  required: ["_id", "name"],
                  properties: {
                    _id: {
                      bsonType: "int"
                    },
                    name: {
                      bsonType: "string",
                      maxLength: 50
                    },
                    dane_code: {
                      bsonType: "int"
                    }
                  },
                  additionalProperties: false
                },
                name: {
                  bsonType: "string",
                  maxLength: 50
                },
                dane_code: {
                  bsonType: "int"
                }
              },
              additionalProperties: false
            },
            direction: {
              bsonType: "binData",
              description: "Encrypted street address"
            },
            created_at: {
              bsonType: "date",
              description: "Address creation timestamp"
            },
            modified_at: {
              bsonType: "date",
              description: "Address modification timestamp"
            }
          },
          additionalProperties: false
        },
        sectionalId: {
          bsonType: "int",
          description: "Sectional identifier"
        },
        position_name: {
          bsonType: "string",
          maxLength: 100,
          description: "Employee position name, max 100 characters"
        },
        created_at: {
          bsonType: "date",
          description: "Creation timestamp"
        },
        modified_at: {
          bsonType: "date",
          description: "Last modification timestamp"
        }
      },
      additionalProperties: false
    }
  },
  validationLevel: "strict",
  validationAction: "error"
});
print("  ✓ 'employees' created.");

// ---------------------------------------------------------------------------
// Create indexes
// ---------------------------------------------------------------------------
print("");
print("==========================================");
print(" Creating Indexes");
print("==========================================");

db.users.createIndex({ "email_searchable": 1 }, { unique: true, name: "idx_users_email_searchable" });
db.users.createIndex({ "identification_number_searchable": 1 }, { unique: true, name: "idx_users_id_number_searchable" });
db.users.createIndex({ "phone_searchable": 1 }, { unique: true, name: "idx_users_phone_searchable" });
db.users.createIndex({ "role": 1 }, { name: "idx_users_role" });

db.owners.createIndex({ "alternative_email_searchable": 1 }, { unique: true, sparse: true, name: "idx_owners_alt_email_searchable" });

db.municipalities.createIndex({ "department._id": 1 }, { name: "idx_municipalities_department" });
db.municipalities.createIndex({ "dane_code": 1 }, { unique: true, name: "idx_municipalities_dane_code" });

print("  ✓ Indexes created.");

print("");
print("==========================================");
print(" ✅ User Service DB initialization complete");
print("==========================================");

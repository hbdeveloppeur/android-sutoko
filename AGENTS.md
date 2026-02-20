# AGENTS.md
Always build debug no cache to test.
Your first step is to read AGENTS_ACTIONS.txt, 
Find the best plan for the asked task.

# Keeping me award of the progression.
- When doing an action, add a json line of a very short explanation of the action you are doing in file ./AGENTS_ACTIONS.txt.

## Instructions
- You are a senior expert: write clean, scalable code with no bad workarounds.
- Respect scream architecture, do not overcomment.

## Fixing Issues

- Create and run temporary executable code to validate changes.
- Reunite the linked files, analyse them, find the issue, fix it.


## UI
When refactoring, keep the same UI visual, previlegy code compatible with Live Edit. 

## Safe resource

- If you init resources, do not forget to free it. For instance docker swarm service when testing
  must be deleted.

## Prod: About the project

- This project uses Docker Compose, you can use "Make up", "make down", "make init".

### At the end - suppress the temporary file you created for testing purpose.

## Clean code, clean structure, scream architecture, clean state management

## Docx Reports are generated in a directory /studies
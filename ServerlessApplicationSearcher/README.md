# ServerlessApplicationSearcher

The program requires a GitHub token connected with a GitHub account. The token is saved as a system variable 'GITHUB_API_TOKEN'.

The results are saved in the following folder structure:

```bash
├───results_<yyyy-MM-dd_HH-mm-ss>
│   ├───projectInfos  # the results of the search with repository info (step 2)
│   ├───projectInfoWithServerless # the results of the search with repository info and serverless file info (step 3)
│   └───searchResults # the results of the search (step 1)

```
Each page of the search is saved in its own file.

Tested with JDK 17.0.8
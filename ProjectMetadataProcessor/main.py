import json
import os
import random
import pathlib
from datetime import datetime
from pytz import timezone

import pandas as pd

# Taken from https://stackoverflow.com/questions/61068988/making-os-file-crawler
def get_filepaths(directory):
    """
    This function will generate the file names in a directory
    tree by walking the tree either top-down or bottom-up. For each
    directory in the tree rooted at directory top (including top itself),
    it yields a 3-tuple (dirpath, dirnames, filenames).
    """
    file_paths = []  # List which will store all the full filepaths.

    # Walk the tree.
    for root, directories, files in os.walk(directory):
        for filename in files:
            # Join the two strings in order to form the full filepath.
            filepath = os.path.join(root, filename)
            file_paths.append(filepath)  # Add it to the list.

    return file_paths


def loadData(directory):
    print(f'Loading data from {directory}')
    filepaths = get_filepaths(directory)
    dfs = []
    for file in filepaths:
        with open(file, encoding="utf-8") as f:
            print(f'file: {file}')
            dfs.append(pd.read_json(f))
    return pd.concat(dfs, ignore_index=True)


def merge_JsonFiles(directory):
    print(f'Loading data from {directory}')
    filepaths = get_filepaths(directory)
    result = list()
    for f1 in filepaths:
        with open(f1, 'r', encoding="utf-8") as infile:
            result.extend(json.load(infile))

    with open('allProjectInfo.json', 'w', encoding="utf-8") as output_file:
        json.dump(result, output_file)


def calcTimeSince(timestring, current_datetime):
    isoformatted = timestring.replace("Z", "+00:00")
    return current_datetime - datetime.fromisoformat(isoformatted)


def getUniqueAndDuplicates(items):
    df = pd.DataFrame(items)
    df_unique = df[~df.astype(str).duplicated()]
    df_duplicated = df[df.astype(str).duplicated()]
    return df_unique, df_duplicated


def add_age_and_last_pushed(dataframe, current_datetime):
    dataframe['ageInDays'] = dataframe['createdAt'].apply(lambda x: calcTimeSince(x, current_datetime).days)
    dataframe['sinceLastPushedInDays'] = dataframe['pushedAt'].apply(lambda x: calcTimeSince(x, current_datetime).days)


def print_to_file(df, filepath):
    with open(filepath, 'w', encoding="utf-8") as file:
        json.dump(json.loads(df.to_json(orient='records')), file, indent=4, sort_keys=True)


def main():
    # Step 1: alle Searchresults einlesen
    ## Pfad zum Ordner mit Json-Dateien der Searchresults angeben
    data = loadData('projectInfoWithServerless')
    result_dir = pathlib.Path('result' + str(datetime.now().strftime("%Y%m%d-%H%M%S")))
    result_dir.mkdir(parents=True, exist_ok=True)
    print('All search results: ' + str(len(data)))
    ## Step 2: Duplikate finden (komplett gleiche Searchresults, nicht nur gleiches Repository)
    df_unique, df_duplicated = getUniqueAndDuplicates(data)
    print('Unique search results: ' + str(len(df_unique)))
    print('Duplicated search results: ' + str(len(df_duplicated)))
    ## Step 3: Nur Suchergebnisse mit Dateinamen "serverless.yml" behalten
    unique_dict = json.loads(df_unique.to_json(orient='records'))
    serverless_only = [x for x in unique_dict if x['serverlessFileInfo']['name'] == 'serverless.yml']
    print('Only with serverless.yml: ' + str(len(serverless_only)))
    ## Step 4: Berechnete Eigenschaften ergänzen
    serverless_only_df = pd.DataFrame(serverless_only)
    add_age_and_last_pushed(serverless_only_df, datetime(2022, 12, 2, 14, 23, 15, 0, tzinfo=timezone('UTC')))

    ## Nur ein Result pro Repository für Repository Metadata verwenden
    unique_repos_df = serverless_only_df[~serverless_only_df.astype(str).duplicated(subset='apiUrl')]
    print('Unique repositories with serverless.yml: ' + str(len(unique_repos_df)))
    print_to_file(unique_repos_df, result_dir.joinpath('repos_unique_serverless.json'))

    # Step 5: Analysemenge zufällig auswählen aus Searchresults mit Dateinamen "serverless.yml" und Runtime NodeJS
    serverless_only_with_computed = json.loads(serverless_only_df.to_json(orient='records'))
    with_runtime = [x for x in serverless_only_with_computed if 'runtime' in x['serverlessFileInfo']]
    print("Search results with runtime:" + str(len(with_runtime)))
    nodejs_only = [x for x in with_runtime if 'nodejs' in x['serverlessFileInfo']['runtime']]
    print('Search results with nodejs runtime: ' + str(len(nodejs_only)))

    selected = random.sample(nodejs_only, 30)

    with open(result_dir.joinpath('selected.json'), 'w', encoding="utf-8") as file:
        json.dump(selected, file, indent=4, sort_keys=True)

    ## Nur ein Result pro Repository für Stichprobe Repository Metadata verwenden
    selected_df = pd.read_json(json.dumps(selected))
    unique_sample_repos_df = selected_df[~selected_df.astype(str).duplicated(subset='apiUrl')]
    print('Unique repos in sample: ' + str(len(unique_sample_repos_df)))
    print_to_file(unique_sample_repos_df, result_dir.joinpath('sample_repos_withoutduplicates.json'))


    print('Done.')


if __name__ == '__main__':
    main()

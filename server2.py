from hanspell import spell_checker
from flask import Flask, jsonify, request


import requests
import json
import re
from konlpy.tag import Hannanum


app = Flask(__name__)

hannanum = Hannanum()


def read_word_list(file_path):  #파일 오픈
    
    word_dict = {}
    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            word, antonym = line.strip().split('\t')[:2]
            if word not in word_dict:
                word_dict[word] = [antonym]
            else:
                word_dict[word].append(antonym)
    return word_dict

def find_antonym(word_dict, word):
    return word_dict.get(word, [])

def show_antonyms(word_dict, word):
    antonyms = find_antonym(word_dict, word)
    if antonyms:
        return antonyms
    else:
        return "{word}의 반의어를 찾을 수 없습니다."
       
# 위 코드는 반의어 데이터 셋을 이용해 반의어를 찾는 코드

def read_similer_list(file_path):  #파일 오픈
    
    word_dict1 = {}
    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            word, simailar = line.strip().split('\t')[:2]
            if word not in word_dict1:
                word_dict1[word] = [simailar]
            else:
                word_dict1[word].append(simailar)
    return word_dict1

def find_simailar(word_dict1, word):
    return word_dict1.get(word, [])

def show_simailar(word_dict1, word):
    simailar = find_simailar(word_dict1, word)
    if simailar:
        return simailar
    else:
        return "{word}의 유의어를 찾을 수 없습니다."
# 위 코드는 반의어 데이터 셋을 이용해 유의어를 찾는 코드



@app.route('/spell', methods=['GET'])

def board_view():

    text = request.args.get('text')
    spelled_sent = spell_checker.check(text)

    response = {
        'before_word': text,
        'after_word': spelled_sent.checked
    }
    
    print('Received Request:', request.method, request.url)
    print('Request Data:', request.args)
    print('Response:', response)
    return jsonify(response), 200
    return jsonify(response), 200


   #"김형호 영화시장분석가는'1987'의 네이버 영화정보 네티즌 10점 평에서 언급된 단어들을 지난해 12월 27일부터 올해 1월 10일까지 통계 프로그램 R과 KoNLP 패키지로 텍스트 마이닝 하여 분석했다.",


@app.route('/words', methods=['GET'])
def board_view1():

    text = request.args.get('text')
    print(text)
    spelled_sent = spell_checker.check(text)
    nouns = hannanum.nouns(spelled_sent.checked)

    response = {
        "before" : text,
        "after" : nouns
    }
    
    print('Received Request:', request.method, request.url)
    print('Request Data:', request.args)
    print('Response:', response)
    return jsonify(response), 200
    return jsonify(response), 200

@app.route('/syso', methods=['GET'])


def symorus():
    text = request.args.get('text')

    url = f"https://opendict.korean.go.kr/api/search?key=7A837117715AE3B41BB3EE9C92C03CC5&q={text}&req_type=json&advanced=y&type1=word"

    payload = {}
    headers = {
    'Cookie': 'GTSID=OPD2&&CyT5kF6B1MnnVRnFkXyZKK6JLx3p8nLhNv0tZtrG1QGhVF2s3ntF!153665181!1682307649001; WMONID=DXPkOHIzCiK; opendic=CyT5kF6B1MnnVRnFkXyZKK6JLx3p8nLhNv0tZtrG1QGhVF2s3ntF!153665181'
    }

    response = requests.request("GET", url, headers=headers, data=payload, verify=False)
    

    # print(response.text)

    

    word_dict = read_word_list('word_list.txt')

# sentence = input("문장을 입력하세요: ")
    words = re.findall(r'\b[가-힣]+\b', text)

    for word in words:
        if word in word_dict:
            antonyms = show_antonyms(word_dict, word)
        else:
            antonyms = "반의어를 찾을 수 없습니다."
    print()


    

    word_dict1 = read_word_list('similer_list.txt')

# sentence = input("문장을 입력하세요: ")
    words = re.findall(r'\b[가-힣]+\b', text)

    for word in words:
        if word in word_dict1:
            simailar = show_simailar(word_dict1, word)
        else:
            simailar = "유의어를 찾을 수 없습니다."
    print()
    

    json_object = json.loads(response.text)
    word = []
    definition = []
    code = []

    # if "item" in json_object['channel']:
    #     for i in range(0, len(json_object['channel']['item'])):
    #         word.append(json_object['channel']['item'][i]['word']) #입력한 단어 - 유의어
    #         definition.append(json_object['channel']['item'][i]['sense'][0]['definition']) # 뜻

    # else:
    #     err = noun, ' 에러'
        
    if "item" in json_object['channel']:
        for i in range(0, 3):
                try:      #리스트의 인덱스가 넘어가는지 확인.
                    word.append(json_object['channel']['item'][i]['word']) #입력한 단어 - 유의어
                    definition.append(json_object['channel']['item'][i]['sense'][0]['definition']) # 뜻
                    code.append(json_object['channel']['item'][i]['sense'][0]["target_code"])
                except IndexError:
                    word.append("None")
                    definition.append("None")
                    code.append("None")



    response = {

        'first_word': word[0],
        'first_definition': definition[0],
        'first_code' : code[0],
        'second_word': word[1],
        'second_definition': definition[1],
        'second_code' : code[1],
        'third_word': word[2],
        'third_definition': definition[2],
        'third_code' : code[2],
        'antonyms' : antonyms,
        'similar' : simailar
    }
    print('Received Request:', request.method, request.url)
    print('Request Data:', request.args)
    print('Response:', response)
    return jsonify(response), 200

    
    print('반의어와 유의어가 리스트에 값이 있습니다.')
    
    print(response)
    return jsonify(response), 200


app.run(host="10.20.32.124",port=5001)
